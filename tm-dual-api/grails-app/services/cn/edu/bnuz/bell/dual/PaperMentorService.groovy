package cn.edu.bnuz.bell.dual

import cn.edu.bnuz.bell.http.BadRequestException
import cn.edu.bnuz.bell.http.ForbiddenException
import cn.edu.bnuz.bell.http.NotFoundException
import cn.edu.bnuz.bell.organization.Teacher
import cn.edu.bnuz.bell.security.SecurityService
import cn.edu.bnuz.bell.security.User
import cn.edu.bnuz.bell.service.DataAccessService
import cn.edu.bnuz.bell.workflow.DomainStateMachineHandler
import cn.edu.bnuz.bell.workflow.ListCommand
import cn.edu.bnuz.bell.workflow.ListType
import cn.edu.bnuz.bell.workflow.State
import cn.edu.bnuz.bell.workflow.WorkflowActivity
import cn.edu.bnuz.bell.workflow.WorkflowInstance
import cn.edu.bnuz.bell.workflow.Workitem
import cn.edu.bnuz.bell.workflow.commands.AcceptCommand
import cn.edu.bnuz.bell.workflow.commands.RejectCommand
import grails.gorm.transactions.Transactional

@Transactional
class PaperMentorService {
    SecurityService securityService
    DataAccessService dataAccessService
    DomainStateMachineHandler domainStateMachineHandler
    ApplicationFormService applicationFormService

    def list(String userId, ListCommand cmd) {
        switch (cmd.type) {
            case ListType.TODO:
                return [forms: findTodoList(userId, cmd.args), counts: getCounts(userId)]
            case ListType.DONE:
                return [forms: findDoneList(userId, cmd.args), counts: getCounts(userId)]
            default:
                throw new BadRequestException()
        }
    }

    def findTodoList(String teacherId, Map args) {
        DegreeApplication.executeQuery '''
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
join form.approver approver
join form.award award
left join form.paperApprover paperApprover
where
approver.id = :teacherId 
and form.status = :status
and current_date between award.requestBegin and award.approvalEnd
order by form.datePaperSubmitted
''', [teacherId: teacherId, status: State.STEP3], args
    }

    def findDoneList(String teacherId, Map args) {
        DegreeApplication.executeQuery '''
select new map(
    form.id as id,
    student.id as studentId,
    student.name as studentName,
    student.sex as sex,
    adminClass.name as className,
    paperApprover.name as paperApprover,
    form.dateSubmitted as date,
    form.status as status
)
from DegreeApplication form
join form.student student
join student.adminClass adminClass
join form.approver approver
join form.paperApprover paperApprover
where form.award.id = :award
and approver.id = :teacherId 
and paperApprover.id is not null
and form.status <> :status
order by form.datePaperApproved desc
''', [award: applicationFormService.latestAward, teacherId: teacherId, status: State.STEP3], args
    }

    def countTodoList(String teacherId) {
        dataAccessService.getLong '''
select count(*)
from DegreeApplication form 
join form.approver approver
join form.award award
left join form.paperApprover paperApprover
where approver.id = :teacherId 
and form.status = :status
and current_date between award.requestBegin and award.approvalEnd
''', [teacherId: teacherId, status: State.STEP3]
    }

    def countDoneList(String teacherId) {
        dataAccessService.getLong '''
select count(*)
from DegreeApplication form 
join form.approver approver
join form.paperApprover paperApprover
where form.award.id = :award
and form.status <> :status
and approver.id = :teacherId
and paperApprover.id is not null
''', [award: applicationFormService.latestAward, teacherId: teacherId, status: State.STEP3]
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
                WorkflowActivity.load("${DegreeApplication.WORKFLOW_ID}.checkPaper"),
                User.load(teacherId),
        )
        if (form.approverId != teacherId) {
            throw new BadRequestException()
        }

        return [
                form: form,
                counts: getCounts(teacherId),
                workitemId: workitem ? workitem.id : null,
                settings: Award.get(form.awardId),
                fileNames: applicationFormService.findFiles(form.studentId, form.awardId),
                prevId: getPrevReviewId(teacherId, id, type),
                nextId: getNextReviewId(teacherId, id, type),
                paperForm: getPaperForm(id),
        ]
    }

    def getFormForReview(String teacherId, Long id, ListType type, UUID workitemId) {
        def form = applicationFormService.getFormInfo(id)

        if (form.paperApproverId != teacherId && form.approverId != teacherId) {
            throw new BadRequestException()
        }

        return [
                form: form,
                counts: getCounts(teacherId),
                workitemId: workitemId,
                settings: Award.get(form.awardId),
                fileNames: applicationFormService.findFiles(form.studentId, form.awardId),
                prevId: getPrevReviewId(teacherId, id, type),
                nextId: getNextReviewId(teacherId, id, type),
                paperForm: getPaperForm(id),
        ]
    }

    private Map getPaperForm(Long mainFormId) {
        def result = DegreeApplication.executeQuery'''
select new map(
    p.type as type,
    p.chineseTitle as chineseTitle,
    p.englishTitle as englishTitle,
    p.name as name
) 
from DegreeApplication da 
join da.paperForm p where da.id = :id
''', [id: mainFormId]
        if (result) {
            return result[0]
        }
        return null
    }

    private Long getPrevReviewId(String teacherId, Long id, ListType type) {
        switch (type) {
            case ListType.TODO:
                return dataAccessService.getLong('''
select form.id
from DegreeApplication form
join form.approver approver
where approver.id = :teacherId 
and form.status = :status
and form.datePaperSubmitted < (select datePaperSubmitted from DegreeApplication where id = :id)
order by form.datePaperSubmitted desc
''', [teacherId: teacherId, id: id, status: State.STEP3])
            case ListType.DONE:
                return dataAccessService.getLong('''
select form.id
from DegreeApplication form
where form.approver.id = :teacherId
and form.status = :status
and form.datePaperSubmitted < (select datePaperSubmitted from DegreeApplication where id = :id)
order by form.datePaperSubmitted desc
''', [teacherId: teacherId, id: id, status: State.STEP4])
        }
    }

    private Long getNextReviewId(String teacherId, Long id, ListType type) {
        switch (type) {
            case ListType.TODO:
                return dataAccessService.getLong('''
select form.id
from DegreeApplication form
join form.approver approver
where approver.id = :teacherId 
and form.status = :status
and form.datePaperSubmitted > (select datePaperSubmitted from DegreeApplication where id = :id)
order by form.datePaperSubmitted asc
''', [teacherId: teacherId, id: id, status: State.STEP3])
            case ListType.DONE:
                return dataAccessService.getLong('''
select form.id
from DegreeApplication form
where form.approver.id = :teacherId
and form.status = :status
and form.datePaperSubmitted > (select datePaperSubmitted from DegreeApplication where id = :id)
order by form.datePaperSubmitted asc
''', [teacherId: teacherId, id: id, status: State.STEP4])
        }
    }

    def tousers() {
        Mentor.executeQuery'''
select new map(
    t.id as id,
    t.name as name
)
from Mentor m 
join m.teacher t 
join m.department d
where d.id = :department
''', [department: securityService.departmentId]
    }

    void next(String userId, AcceptCommand cmd, UUID workitemId) {
        DegreeApplication form = DegreeApplication.get(cmd.id)
        if (form.award.betweenCheckDateRange()) {
            domainStateMachineHandler.next(form, userId, 'approvePaper', cmd.comment, workitemId, cmd.to)
            form.paperApprover = Teacher.load(cmd.to)
            form.save()
        }
    }

    def setMentor(String userId, PaperMentorCommand cmd) {
        cmd.ids.each { id ->
            def form = DegreeApplication.get(id)
            if (form && form.award.betweenCheckDateRange()) {
                def workitem = Workitem.findByInstanceAndActivityAndToAndDateProcessedIsNull(
                        WorkflowInstance.load(form.workflowInstanceId),
                        WorkflowActivity.load("${DegreeApplication.WORKFLOW_ID}.checkPaper"),
                        User.load(userId),
                )

                domainStateMachineHandler.next(form, userId, 'approvePaper', null, workitem.id, cmd.teacherId)
                form.paperApprover = Teacher.load(cmd.teacherId)
                form.save()
            }
        }
    }

    void reject(String teacherId, RejectCommand cmd, UUID id) {
        DegreeApplication form = DegreeApplication.get(cmd.id)
        if (form.approver.id != teacherId) {
            throw new BadRequestException()
        }
        if (form.award.betweenCheckDateRange()) {
            domainStateMachineHandler.reject(form, teacherId, 'checkPaper', cmd.comment, id)
            form.paperApprover = null
            form.save()
        }
    }

    def finish(String teacherId, FinishCommand cmd) {
        DegreeApplication form = DegreeApplication.get(cmd.id)

        if (!form || !form.paperForm) {
            throw new NotFoundException()
        }

        if (form.approver.id != teacherId && form.paperApprover.id != teacherId) {
            throw new ForbiddenException()
        }

        if (!domainStateMachineHandler.canFinish(form)) {
            throw new BadRequestException()
        }

        def workitem = Workitem.findByInstanceAndActivityAndToAndDateProcessedIsNull(
                WorkflowInstance.load(form.workflowInstanceId),
                WorkflowActivity.load('dual.application.checkPaper'),
                User.load(teacherId),
        )

        if (form.award.betweenCheckDateRange()) {
            domainStateMachineHandler.finish(form, teacherId, workitem.id)
            form.datePaperApproved = new Date()
            form.paperForm.comment = cmd.comment
            form.save()
        }
    }
}
