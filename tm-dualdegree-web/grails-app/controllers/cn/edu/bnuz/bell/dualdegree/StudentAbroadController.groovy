package cn.edu.bnuz.bell.dualdegree

import org.springframework.security.access.prepost.PreAuthorize

@PreAuthorize('hasRole("ROLE_DUALDEGREE_ADMIN_DEPT")')
class StudentAbroadController {

    def index() { }
}
