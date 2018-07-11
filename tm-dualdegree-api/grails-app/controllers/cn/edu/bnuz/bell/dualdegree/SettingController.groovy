package cn.edu.bnuz.bell.dualdegree

import cn.edu.bnuz.bell.organization.DepartmentService
import org.springframework.security.access.prepost.PreAuthorize

@PreAuthorize('hasAuthority("PERM_DUALDEGREE_ADMIN")')
class SettingController {
    SettingService settingService
    DepartmentService departmentService

    def index() {
        renderJson(settingService.list())
    }

    /**
     * 保存数据
     */
    def save() {
        def cmd = new DeptAdministratorCommand()
        bindData(cmd, request.JSON)
        def form = settingService.create(cmd)
        renderJson([id: form.id])
    }

    /**
     * 创建
     */
    def create() {
        renderJson([
                form: [],
                departments: departmentService.teachingDepartments
        ])
    }

    /**
     * 删除
     */
    def delete(Long id) {
        settingService.delete(id)
        renderOk()
    }
}
