package cn.edu.bnuz.bell.dual

import org.springframework.security.access.prepost.PreAuthorize

@PreAuthorize('hasRole("ROLE_DUALDEGREE_STUDENT")')
class AwardPublicController {
    AwardPublicService awardPublicService

    def show( Long id) {
        renderJson awardPublicService.getAwardInfo(id)
    }
}
