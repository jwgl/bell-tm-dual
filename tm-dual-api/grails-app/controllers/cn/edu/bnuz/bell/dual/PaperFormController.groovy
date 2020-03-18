package cn.edu.bnuz.bell.dual

import cn.edu.bnuz.bell.http.BadRequestException
import cn.edu.bnuz.bell.http.ForbiddenException
import cn.edu.bnuz.bell.http.NotFoundException
import cn.edu.bnuz.bell.http.ServiceExceptionHandler
import cn.edu.bnuz.bell.workflow.Event
import cn.edu.bnuz.bell.workflow.State
import cn.edu.bnuz.bell.workflow.commands.AcceptCommand
import org.springframework.security.access.prepost.PreAuthorize

import java.time.LocalDate


@PreAuthorize('hasRole("ROLE_DUALDEGREE_STUDENT")')
class PaperFormController implements ServiceExceptionHandler {
    ApplicationFormService applicationFormService
    PaperFormService paperFormService

    def index(String studentId, Long applicationFormId) {
        renderJson([form: paperFormService.getPaperForm(studentId, applicationFormId)])
    }

    def save(String studentId, Long applicationFormId) {
        def cmd = new PaperFormCommand()
        bindData(cmd, request.JSON)
        // 如果预期，不做任何操作
        DegreeApplication application = DegreeApplication.load(applicationFormId)
        if (isExpire(application)) {
            renderForbidden()
        } else {
            def form = paperFormService.create(studentId, applicationFormId, cmd)
            renderJson([id: form.id])
        }
    }

    def tousers(String studentId, Long applicationFormId) {
        renderJson paperFormService.getUser(applicationFormId)
    }

    def patch(String studentId, Long applicationFormId, String id, String op) {
        // 如果预期，不做任何操作
        def application = DegreeApplication.load(applicationFormId)
        if (isExpire(application)) {
            throw new ForbiddenException()
        }
        def fileNames = applicationFormService.findFiles(studentId, application.awardId)
        if (!fileNames.paper) {
            throw new BadRequestException('还未上传论文！')
        }
        def operation = Event.valueOf(op)
        switch (operation) {
            case Event.NEXT:
                def cmd = new AcceptCommand()
                bindData(cmd, request.JSON)
                cmd.id = applicationFormId
                def users = paperFormService.getUser(applicationFormId)
                if (!users) {
                    throw new NotFoundException()
                }
                cmd.to = users[0].id
                paperFormService.next(studentId, cmd, id)
                break
            default:
                throw new BadRequestException()
        }

        renderJson applicationFormService.getFormForShow(studentId, applicationFormId)
    }

    private static Boolean isExpire(DegreeApplication application) {
        def now = LocalDate.now()
        if ((application.status == State.STEP2 && now.isAfter(application.award.paperEnd)) ||
                (application.status == State.STEP5 && now.isAfter(application.award.approvalEnd))) {
            return true
        }
    }
}
