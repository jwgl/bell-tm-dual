package cn.edu.bnuz.bell.dualdegree

import cn.edu.bnuz.bell.http.BadRequestException
import cn.edu.bnuz.bell.workflow.Event
import cn.edu.bnuz.bell.workflow.ListCommand
import cn.edu.bnuz.bell.workflow.ListType
import cn.edu.bnuz.bell.workflow.commands.AcceptCommand
import cn.edu.bnuz.bell.workflow.commands.RejectCommand
import org.springframework.security.access.prepost.PreAuthorize

@PreAuthorize('hasAuthority("PERM_DUALDEGREE_DEPT_ADMIN")')
class ApplicationCheckController {
    ApplicationCheckService applicationCheckService
    ApplicationReviewerService applicationReviewerService

    def index(String checkerId, ListCommand cmd) {
        renderJson(applicationCheckService.list(checkerId, cmd))
    }

    def update(String checkerId, Long id) {
        def cmd = new MentorCommand()
        bindData(cmd, request.JSON)
        applicationCheckService.setPaperApprover(id, cmd.teacherId)
        renderOk()
    }

    def show(String checkerId, Long applicationCheckId, String id, String type) {
        ListType listType = ListType.valueOf(type)
        if (id == 'undefined') {
            renderJson applicationCheckService.getFormForReview(checkerId, applicationCheckId, listType)
        } else {
            renderJson applicationCheckService.getFormForReview(checkerId, applicationCheckId, listType, UUID.fromString(id))
        }
    }

    def patch(String checkerId, Long applicationCheckId, String id, String op) {
        def operation = Event.valueOf(op)
        switch (operation) {
            case Event.NEXT:
                def cmd = new AcceptCommand()
                bindData(cmd, request.JSON)
                cmd.id = applicationCheckId
                applicationCheckService.next(checkerId, cmd, UUID.fromString(id))
                break
            case Event.REJECT:
                def cmd = new RejectCommand()
                bindData(cmd, request.JSON)
                cmd.id = applicationCheckId
                applicationCheckService.reject(checkerId, cmd, UUID.fromString(id))
                break
            default:
                throw new BadRequestException()
        }

        show(checkerId, applicationCheckId, id, 'todo')
    }

    def proposer(String checkerId, Long applicationCheckId) {
        renderJson applicationReviewerService.getProposer(applicationCheckId)
    }

    def tousers(String checkerId, Long applicationCheckId) {
        renderJson applicationCheckService.tousers(applicationCheckId)
    }
}
