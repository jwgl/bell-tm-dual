package cn.edu.bnuz.bell.dualdegree

import org.springframework.security.access.prepost.PreAuthorize

@PreAuthorize('hasAuthority("PERM_DUALDEGREE_DEPT_ADMIN")')
class ApplicationCheckController {

    def index() { }
}
