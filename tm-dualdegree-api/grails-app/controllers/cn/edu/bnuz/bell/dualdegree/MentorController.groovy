package cn.edu.bnuz.bell.dualdegree

import org.springframework.security.access.prepost.PreAuthorize

@PreAuthorize('hasAuthority("PERM_DUALDEGREE_DEPT_ADMIN")')
class MentorController {
    MentorService mentorService

    def index(String departmentId) {
        renderJson mentorService.list()
    }

    def save(String departmentId) {
        def cmd = new MentorCommand()
        bindData(cmd, request.JSON)
        def form = mentorService.create(cmd)
        renderJson([id: form.id])
    }

    def edit(String departmentId, Long id) {
        renderJson mentorService.getFormForEdit(id)
    }

    /**
     * 更新数据
     */
    def update(String departmentId, Long id) {
        def cmd = new MentorCommand()
        bindData(cmd, request.JSON)
        mentorService.update(id, cmd)
        renderOk()
    }

    /**
     * 删除
     */
    def delete(Long id) {
        mentorService.delete(id)
        renderOk()
    }
}
