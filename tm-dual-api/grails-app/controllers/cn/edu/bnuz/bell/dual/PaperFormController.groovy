package cn.edu.bnuz.bell.dual

import cn.edu.bnuz.bell.http.BadRequestException
import cn.edu.bnuz.bell.workflow.Event
import cn.edu.bnuz.bell.workflow.commands.AcceptCommand
import org.springframework.security.access.prepost.PreAuthorize


@PreAuthorize('hasRole("ROLE_DUALDEGREE_STUDENT")')
class PaperFormController {
    ApplicationFormService applicationFormService
    PaperFormService paperFormService

    def index(String studentId, Long applicationFormId) {
        renderJson([form: paperFormService.getPaperForm(studentId, applicationFormId)])
    }

    def save(String studentId, Long applicationFormId) {
        def cmd = new PaperFormCommand()
        bindData(cmd, request.JSON)
        def form = paperFormService.create(studentId, applicationFormId, cmd)
        renderJson([id: form.id])
    }

    def tousers(String studentId, Long applicationFormId) {
        renderJson paperFormService.getUser(applicationFormId)
    }

    def patch(String studentId, Long applicationFormId, String id, String op) {
        def operation = Event.valueOf(op)
        switch (operation) {
            case Event.NEXT:
                def cmd = new AcceptCommand()
                bindData(cmd, request.JSON)
                cmd.id = applicationFormId
                paperFormService.next(studentId, cmd, id)
                break
            default:
                throw new BadRequestException()
        }

        renderJson applicationFormService.getFormForShow(studentId, applicationFormId)
    }
}
