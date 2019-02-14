package cn.edu.bnuz.bell.dual

import cn.edu.bnuz.bell.http.BadRequestException
import cn.edu.bnuz.bell.http.ForbiddenException
import cn.edu.bnuz.bell.http.NotFoundException
import cn.edu.bnuz.bell.organization.Teacher
import cn.edu.bnuz.bell.security.SecurityService
import cn.edu.bnuz.bell.security.User
import cn.edu.bnuz.bell.security.UserLogService
import cn.edu.bnuz.bell.service.DataAccessService
import cn.edu.bnuz.bell.workflow.Activities
import cn.edu.bnuz.bell.workflow.DomainStateMachineHandler
import cn.edu.bnuz.bell.workflow.ListCommand
import cn.edu.bnuz.bell.workflow.ListType
import cn.edu.bnuz.bell.workflow.State
import cn.edu.bnuz.bell.workflow.WorkflowActivity
import cn.edu.bnuz.bell.workflow.WorkflowInstance
import cn.edu.bnuz.bell.workflow.Workitem
import cn.edu.bnuz.bell.workflow.commands.AcceptCommand
import cn.edu.bnuz.bell.workflow.commands.RejectCommand
import cn.edu.bnuz.bell.workflow.commands.RevokeCommand
import grails.gorm.transactions.Transactional

@Transactional
class ApplicationCheckService {
    DataAccessService dataAccessService
    DomainStateMachineHandler domainStateMachineHandler
    ApplicationFormService applicationFormService
    UserLogService userLogService
    SecurityService securityService

    def list(String userId, ListCommand cmd) {
        switch (cmd.type) {
            case ListType.TODO:
                return findTodoList(userId, cmd.args)
            case ListType.DONE:
                return findDoneList(userId, cmd.args)
            default:
                throw new BadRequestException()
        }
    }

    def findTodoList(String teacherId, Map args) {
        def forms = DegreeApplication.executeQuery '''
select new map(
  form.id as id,
  student.id as studentId,
  student.name as studentName,
  student.sex as sex,
  adminClass.name as className,
  form.dateSubmitted as date,
  form.status as status
)
from DegreeApplication form
join form.student student
join student.adminClass adminClass
where form.approver.id = :teacherId
and form.award.id = :award
and form.status = :status
order by form.dateSubmitted
''', [award: applicationFormService.latestAward, teacherId: teacherId, status: State.STEP1], args

        return [forms: forms, counts: getCounts(teacherId)]
    }

    def findDoneList(String teacherId, Map args) {
        def forms = DegreeApplication.executeQuery '''
select new map(
  form.id as id,
  student.id as studentId,
  student.name as studentName,
  student.sex as sex,
  adminClass.name as className,
  form.dateSubmitted as date,
  paperApprover.name as paperApprover,
  form.status as status
)
from DegreeApplication form
join form.student student
join student.adminClass adminClass
left join form.paperApprover paperApprover
where form.approver.id = :teacherId
and form.award.id = :award
and form.dateApproved is not null
and form.status <> :status
order by form.dateApproved desc
''', [award: applicationFormService.latestAward, teacherId: teacherId, status: State.STEP1], args

        return [forms: forms, counts: getCounts(teacherId)]
    }

    def countTodoList(String teacherId) {
        dataAccessService.getLong '''
select count(*)
from DegreeApplication form 
where form.award.id = :award
and form.status = :status
and form.approver.id = :teacherId
''', [award: applicationFormService.latestAward, teacherId: teacherId, status: State.STEP1]
    }

    def countDoneList(String teacherId) {
        dataAccessService.getLong '''
select count(*)
from DegreeApplication form
where form.award.id = :award
and form.status <> :status
and form.dateApproved is not null
and form.approver.id = :teacherId
''', [award: applicationFormService.latestAward, teacherId: teacherId, status: State.STEP1]
    }

    def getCounts(String teacherId) {
        [
                (ListType.TODO): countTodoList(teacherId),
                (ListType.DONE): countDoneList(teacherId),
        ]
    }

    def getFormForReview(String teacherId, Long id, ListType type) {
        def form = applicationFormService.getFormInfo(id)

        def workitem = Workitem.findByInstanceAndActivityAndToAndDateProcessedIsNull(
                WorkflowInstance.load(form.workflowInstanceId),
                WorkflowActivity.load("${DegreeApplication.WORKFLOW_ID}.${Activities.CHECK}"),
                User.load(teacherId),
        )
        domainStateMachineHandler.checkReviewer(id, teacherId, Activities.CHECK)

        return [
                form: form,
                counts: getCounts(teacherId),
                workitemId: workitem ? workitem.id : null,
                settings: Award.get(form.awardId),
                fileNames: applicationFormService.findFiles(form.studentId, form.awardId),
                prevId: getPrevReviewId(teacherId, id, type),
                nextId: getNextReviewId(teacherId, id, type),
        ]
    }

    def getFormForReview(String teacherId, Long id, ListType type, UUID workitemId) {
        def form = applicationFormService.getFormInfo(id)

        def activity = Workitem.get(workitemId).activitySuffix
        domainStateMachineHandler.checkReviewer(id, teacherId, activity)

        return [
                form: form,
                counts: getCounts(teacherId),
                workitemId: workitemId,
                settings: Award.get(form.awardId),
                fileNames: applicationFormService.findFiles(form.studentId, form.awardId),
                prevId: getPrevReviewId(teacherId, id, type),
                nextId: getNextReviewId(teacherId, id, type),
        ]
    }

    private Long getPrevReviewId(String teacherId, Long id, ListType type) {
        switch (type) {
            case ListType.TODO:
                return dataAccessService.getLong('''
select form.id
from DegreeApplication form 
where form.award.id = :award
and form.status = :status
and form.approver.id = :teacherId
and form.dateSubmitted < (select dateSubmitted from DegreeApplication where id = :id)
order by form.dateSubmitted desc
''', [award: applicationFormService.latestAward, teacherId: teacherId, id: id, status: State.STEP1])
            case ListType.DONE:
                return dataAccessService.getLong('''
select form.id
from DegreeApplication form
where form.approver.id = :teacherId
and form.award.id = :award
and form.dateApproved is not null
and form.status <> :status
and form.dateApproved > (select dateApproved from DegreeApplication where id = :id)
order by form.dateApproved asc
''', [award: applicationFormService.latestAward, teacherId: teacherId, id: id, status: State.STEP1])
        }
    }

    private Long getNextReviewId(String teacherId, Long id, ListType type) {
        switch (type) {
            case ListType.TODO:
                return dataAccessService.getLong('''
select form.id
from DegreeApplication form 
where form.award.id = :award
and form.status = :status
and form.approver.id = :teacherId
and form.dateSubmitted > (select dateSubmitted from DegreeApplication where id = :id)
order by form.dateSubmitted asc
''', [award: applicationFormService.latestAward, teacherId: teacherId, id: id, status: State.STEP1])
            case ListType.DONE:
                return dataAccessService.getLong('''
select form.id
from DegreeApplication form
where form.award.id = :award
and form.approver.id = :teacherId
and form.dateApproved is not null
and form.status <> :status
and form.dateApproved < (select dateApproved from DegreeApplication where id = :id)
order by form.dateApproved desc
''', [award: applicationFormService.latestAward, teacherId: teacherId, id: id, status: State.STEP1])
        }
    }

    void next(String userId, AcceptCommand cmd, UUID workitemId) {
        DegreeApplication form = DegreeApplication.get(cmd.id)
        // 判断是否允许下一步
        Workitem workitem = Workitem.load(workitemId)
        if (form.status == State.STEP1 && workitem.to.id == userId && workitem.state == State.STEP1) {
            domainStateMachineHandler.next(form, userId, Activities.CHECK, cmd.comment, workitemId, cmd.to)
            form.dateApproved = new Date()
            form.save()
        }
    }

    void reject(String userId, RejectCommand cmd, UUID workitemId) {
        DegreeApplication form = DegreeApplication.get(cmd.id)
        domainStateMachineHandler.reject(form, userId, Activities.CHECK, cmd.comment, workitemId)
        form.dateApproved = new Date()
        form.save()
    }

    void rollback(String userId, RevokeCommand cmd) {
        DegreeApplication form = DegreeApplication.get(cmd.id)
        if (!form) {
            throw new NotFoundException()
        }
        if (!domainStateMachineHandler.canRollback(form) || (form.status != State.STEP2 && form.status != State.REJECTED)) {
            throw new ForbiddenException()
        }
        // 找到最后workitem并删除
        def workitem = Workitem.findByInstanceAndActivityAndFromAndDateProcessedIsNull(
                WorkflowInstance.load(form.workflowInstanceId),
                WorkflowActivity.load("${DegreeApplication.WORKFLOW_ID}.submitPaper"),
                User.load(userId))
        if (form.status == State.REJECTED) {
            workitem = Workitem.findByInstanceAndActivityAndFromAndDateProcessedIsNull(
                    WorkflowInstance.load(form.workflowInstanceId),
                    WorkflowActivity.load("${DegreeApplication.WORKFLOW_ID}.view"),
                    User.load(userId))
        }

        if (!workitem) {
            throw new BadRequestException()
        }
        workitem.delete()
        domainStateMachineHandler.rollback(form, userId, cmd.comment, workitem.id)
        if (cmd.comment) {
            userLogService.log(securityService.userId, securityService.ipAddress, 'ROLLBACK', form, "撤回理由：${cmd.comment}")
        }
        // 找到最后workitem，恢复标志
        List<Workitem> result = Workitem.executeQuery'''
from Workitem where instance = :instance and activity = :activity and to = :to order by dateCreated desc
''', [instance: WorkflowInstance.load(form.workflowInstanceId), activity: WorkflowActivity.load('dualdegree.application.check'),
      to: User.load(userId)], [max: 1]
        if (!result) {
            throw new BadRequestException()
        }
        def revokeItem = result[0]
        revokeItem.setDateProcessed(null)
        revokeItem.setDateReceived(null)
        revokeItem.setNote(null)
        revokeItem.save()
        form.dateApproved = null
        form.save()
    }

    void setPaperApprover(Long id, String teacherId) {
        def form = DegreeApplication.load(id)
        form.setPaperApprover(Teacher.load(teacherId))
        form.save()
    }

    def tousers(Long id) {
        return applicationFormService.getUser(id)
    }
}
