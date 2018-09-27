package cn.edu.bnuz.bell.dual

import org.springframework.security.access.prepost.PreAuthorize

@PreAuthorize('hasRole("ROLE_DUALDEGREE_ADMIN_DEPT")')
class AwardController {
    AwardService awardService

    def index() {
        renderJson(awardService.list())
    }

    /**
     * 保存数据
     */
    def save() {
        def cmd = new AwardCommand()
        bindData(cmd, request.JSON)
        def form = awardService.create(cmd)
        renderJson([id: form.id])
    }

    /**
     * 编辑数据
     */
    def edit(Long id) {
        renderJson([
                form: awardService.getFormForShow(id),
                departments: awardService.myDepartments
        ])
    }

    def show(Long id) {
        renderJson(awardService.getFormForShow(id))
    }

    /**
     * 创建
     */
    def create() {
        renderJson([
                form: [ ],
                departments: awardService.myDepartments
        ])
    }

    /**
     * 更新数据
     */
    def update(Long id) {
        def cmd = new AwardCommand()
        bindData(cmd, request.JSON)
        cmd.id = id
        awardService.update(cmd)
        renderOk()
    }
}
