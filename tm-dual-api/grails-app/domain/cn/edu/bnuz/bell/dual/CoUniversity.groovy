package cn.edu.bnuz.bell.dual

import cn.edu.bnuz.bell.organization.Department

/**
 * 合作大学列表
 */
class CoUniversity {
    String name
    Department department
    static mapping = {
        comment             '合作大学列表：只适用于15级以前'
        table               schema: 'tm_dual'
        id                  generator: 'identity', comment: '无意义ID'
        name                comment: '可管理部门'
        department          comment: '关联本校部门'
    }
}
