package cn.edu.bnuz.bell.dualdegree

import cn.edu.bnuz.bell.http.BadRequestException
import cn.edu.bnuz.bell.workflow.Event
import cn.edu.bnuz.bell.workflow.ListCommand
import cn.edu.bnuz.bell.workflow.ListType
import cn.edu.bnuz.bell.workflow.commands.AcceptCommand
import cn.edu.bnuz.bell.workflow.commands.RejectCommand
import org.springframework.security.access.prepost.PreAuthorize

@PreAuthorize('hasAuthority("PERM_DUALDEGREE_DEPT_ADMIN")')
class PaperMentorController {
    PaperMentorService paperMentorService
    MentorService mentorService

    def index(String approverId, ListCommand cmd) {
        renderJson(paperMentorService.list(approverId, cmd))
    }

    def show(String approverId, Long paperMentorId, String id, String type) {
        ListType listType = ListType.valueOf(type)
        if (id == 'undefined') {
            renderJson paperMentorService.getFormForReview(approverId, paperMentorId, listType)
        } else {
            renderJson paperMentorService.getFormForReview(approverId, paperMentorId, listType, UUID.fromString(id))
        }
    }

    def save(String approverId) {
        PaperMentorCommand cmd = new PaperMentorCommand()
        bindData(cmd, request.JSON)
        paperMentorService.setMentor(approverId, cmd)
        renderOk()
    }

    def patch(String approverId, Long paperMentorId, String id, String op) {

        def operation = Event.valueOf(op)
        switch (operation) {
            case Event.NEXT:
                def cmd = new AcceptCommand()
                bindData(cmd, request.JSON)
                cmd.id = paperMentorId
                paperMentorService.next(approverId, cmd, UUID.fromString(id))
                break
            case Event.REJECT:
                def cmd = new RejectCommand()
                bindData(cmd, request.JSON)
                cmd.id = paperMentorId
                paperMentorService.reject(approverId, cmd, UUID.fromString(id))
                break
            default:
                throw new BadRequestException()
        }

        show(approverId, paperMentorId, id, 'todo')
    }

    def tousers(String approverId, Long paperMentorId) {
        renderJson paperMentorService.tousers()
    }
}
