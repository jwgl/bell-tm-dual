package cn.edu.bnuz.bell.dualdegree

import cn.edu.bnuz.bell.organization.Department
import cn.edu.bnuz.bell.organization.Teacher

/**
 * 学院管理员
 */
class DepartmentAdministrator {
    Department department
    Teacher teacher

    static mapping = {
        comment         '学院管理员'
        table           schema: 'tm_dual'
        id              generator: 'identity', comment: '无意义ID'
        department      comment: '可管理部门'
        teacher         comment: '教师'
    }

    static constraints = {
        teacher         unique: 'department'
    }
}
