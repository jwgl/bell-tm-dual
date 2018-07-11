package cn.edu.bnuz.bell.dualdegree

import org.springframework.security.access.prepost.PreAuthorize

@PreAuthorize('hasRole("ROLE_DUALDEGREE_STUDENT")')
class ApplicationFormController {

    def index() { }

    def pictures(String studentId, Long awardPublicId) {
    }
}
