package cn.edu.bnuz.bell.dual


import org.springframework.security.access.prepost.PreAuthorize

@PreAuthorize('hasAuthority("PERM_DUALDEGREE_ADMIN")')
class CooperativeUniversityController {
    CooperativeUniversityService cooperativeUniversityService
    def index() {
        renderJson cooperativeUniversityService.list()
    }

    def show(Long id) {
        renderJson(cooperativeUniversityService.getFormForShow(id))
    }

    /**
     * 编辑数据
     */
    def edit(Long id) {
        renderJson([
                form: cooperativeUniversityService.getFormForEdit(id),
                regions: cooperativeUniversityService.regions
        ])
    }

    /**
     * 保存数据
     */
    def save() {
        def cmd = new CooperativeUniversityCommand()
        bindData(cmd, request.JSON)
        def form = cooperativeUniversityService.create(cmd)
        renderJson([id: form.id])
    }

    /**
     * 更新数据
     */
    def update(Long id) {
        def cmd = new CooperativeUniversityCommand()
        bindData(cmd, request.JSON)
        cmd.id = id
        cooperativeUniversityService.update(cmd)
        renderOk()
    }

    /**
     * 创建
     */
    def create() {
        renderJson([
                form: [
                        items: [],
                ],
                regions: cooperativeUniversityService.regions,
        ])
    }
}
