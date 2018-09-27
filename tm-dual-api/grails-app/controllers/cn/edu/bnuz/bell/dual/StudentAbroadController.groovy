package cn.edu.bnuz.bell.dual

import org.springframework.security.access.prepost.PreAuthorize

/**
 * 出国学生管理
 */
@PreAuthorize('hasRole("ROLE_DUALDEGREE_ADMIN_DEPT")')
class StudentAbroadController {
    StudentAbroadService studentAbroadService

    def index() {
        StudentOptionsCommand optionsCommand = new StudentOptionsCommand(
                sujectId: params['subjectId'],
                regionId: params.getInt('regionId') ?: 0,
                grade: params.getInt('grade') ?: 0,
                studentId: params['studentId'],
                studentName: params['studentName']
        )
        renderJson(studentAbroadService.list(optionsCommand))
    }

    /**
     * 保存数据
     */
    def save() {
        def cmd = new StudentAbroadCommand()
        bindData(cmd, request.JSON)
        if (!cmd.studentIds) {
            renderOk()
        } else {
            renderJson([logs: studentAbroadService.create(cmd)])
        }
    }

    /**
     * 创建
     */
    def create() {
        renderJson([
                form: [],
                subjects: studentAbroadService.subjects,
                grades: studentAbroadService.grades,
                agreementRegions: studentAbroadService.agreementRegions
        ])
    }

    /**
     * 删除
     */
    def delete(Long id) {
        studentAbroadService.delete(id)
        renderOk()
    }
}
