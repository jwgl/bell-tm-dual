package cn.edu.bnuz.bell.dualdegree

import cn.edu.bnuz.bell.security.User
import cn.edu.bnuz.bell.workflow.DomainStateMachineHandler
import cn.edu.bnuz.bell.workflow.WorkflowActivity
import cn.edu.bnuz.bell.workflow.WorkflowInstance
import cn.edu.bnuz.bell.workflow.Workitem
import cn.edu.bnuz.bell.workflow.commands.AcceptCommand
import grails.gorm.transactions.Transactional

import javax.swing.text.BadLocationException

@Transactional
class PaperFormService {
    DomainStateMachineHandler domainStateMachineHandler

    def getPaperForm(String studentId, Long applicationFormId) {
        def result = DegreeApplication.executeQuery'''
select new map(
pf.type  as type,
pf.name  as name,
pf.chineseTitle as chineseTitle,
pf.englishTitle as englishTitle
)
from DegreeApplication da 
join da.paperForm pf
where da.id = :id and da.student.id = :studentId
''', [id: applicationFormId, studentId: studentId]
        return result ? result[0] : null
    }

    def create(String studentId, Long applicationFormId, PaperFormCommand cmd) {
        def applicationForm = DegreeApplication.load(applicationFormId)
        if (!applicationForm) {
            throw new BadLocationException()
        }
        PaperForm form = applicationForm.paperForm
        if (!form) {
            form = new PaperForm(
                    name: cmd.name,
                    type: cmd.type,
                    chineseTitle: cmd.chineseTitle,
                    englishTitle: cmd.englishTitle,
                    form: applicationForm
            )
        } else {
            form.name = cmd.name
            form.type = cmd.type
            form.chineseTitle = cmd.chineseTitle
            form.englishTitle = cmd.englishTitle
        }
        form.save()
        applicationForm.setPaperForm(form)
        applicationForm.setDatePaperSubmitted(new Date())
        applicationForm.save()
        return form
    }

    /**
     * 获取论文审核人，如果未设默认为approver
     * @param id
     * @return
     */
    def getUser(Long id) {
        def form = DegreeApplication.load(id)
        def user = form.paperApprover ?: form.approver
        return [[id : user.id, name: user.name]]
    }


    void next(String userId, AcceptCommand cmd, String id) {
        DegreeApplication form = DegreeApplication.get(cmd.id)
        def workitem = Workitem.findByInstanceAndActivityAndToAndDateProcessedIsNull(
                WorkflowInstance.load(form.workflowInstanceId),
                WorkflowActivity.load("${DegreeApplication.WORKFLOW_ID}.process"),
                User.load(userId),
        )
        if (!workitem) {
            workitem = Workitem.findByInstanceAndActivityAndToAndDateProcessedIsNull(
                    WorkflowInstance.load(form.workflowInstanceId),
                    WorkflowActivity.load("${DegreeApplication.WORKFLOW_ID}.view"),
                    User.load(userId),
            )
        }
        domainStateMachineHandler.next(form, userId, 'process', cmd.comment, workitem.id, cmd.to)
        form.datePaperSubmitted = new Date()
        form.save()
    }
}
