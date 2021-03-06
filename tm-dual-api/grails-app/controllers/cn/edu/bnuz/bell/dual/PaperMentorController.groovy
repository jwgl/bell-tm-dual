package cn.edu.bnuz.bell.dual

import cn.edu.bnuz.bell.http.BadRequestException
import cn.edu.bnuz.bell.workflow.Event
import cn.edu.bnuz.bell.workflow.ListCommand
import cn.edu.bnuz.bell.workflow.ListType
import cn.edu.bnuz.bell.workflow.commands.AcceptCommand
import cn.edu.bnuz.bell.workflow.commands.RejectCommand
import cn.edu.bnuz.bell.workflow.commands.RevokeCommand
import org.springframework.security.access.prepost.PreAuthorize

@PreAuthorize('hasAuthority("PERM_DUALDEGREE_DEPT_ADMIN")')
class PaperMentorController {
    PaperMentorService paperMentorService

    def index(String checkerId, ListCommand cmd) {
        renderJson(paperMentorService.list(checkerId, cmd))
    }

    def show(String checkerId, Long paperMentorId, String id, String type) {
        ListType listType = ListType.valueOf(type)
        if (id == 'undefined') {
            renderJson paperMentorService.getFormForReview(checkerId, paperMentorId, listType)
        } else {
            renderJson paperMentorService.getFormForReview(checkerId, paperMentorId, listType, UUID.fromString(id))
        }
    }

    def save(String checkerId) {
        PaperMentorCommand cmd = new PaperMentorCommand()
        bindData(cmd, request.JSON)
        paperMentorService.setMentor(checkerId, cmd)
        renderOk()
    }

    def patch(String checkerId, Long paperMentorId, String id, String op) {
        def operation = Event.valueOf(op)
        // FINISH 操作时id是实体ID，其他操作时id是活动ID
        switch (operation) {
            case Event.NEXT:
                def cmd = new AcceptCommand()
                bindData(cmd, request.JSON)
                cmd.id = paperMentorId
                paperMentorService.next(checkerId, cmd, UUID.fromString(id))
                break
            case Event.REJECT:
                def cmd = new RejectCommand()
                bindData(cmd, request.JSON)
                cmd.id = paperMentorId
                paperMentorService.reject(checkerId, cmd, UUID.fromString(id))
                break
            case Event.FINISH:
                def cmd = new FinishCommand()
                bindData(cmd, request.JSON)
                cmd.id = id as Long
                paperMentorService.finish(checkerId, cmd)
                break
            case Event.ROLLBACK:
                def cmd = new RevokeCommand()
                bindData(cmd, request.JSON)
                cmd.id = paperMentorId
                paperMentorService.rollback(checkerId, cmd)
                break
            default:
                throw new BadRequestException()
        }
        if (operation == Event.FINISH) {
            show(checkerId, id as Long, 'undefined', 'todo')
        } else {
            show(checkerId, paperMentorId, id, 'todo')
        }
    }

    /**
     * @return 返回所有导师列表
     */
    def tousers(String checkerId, Long paperMentorId) {
        renderJson paperMentorService.tousers()
    }

    /**
     * @return 返回导师列表，排除已分配过的导师
     */
    def mentorsExcept(String checkerId, Long paperMentorId) {
        renderJson paperMentorService.mentorsExcept()
    }
}
