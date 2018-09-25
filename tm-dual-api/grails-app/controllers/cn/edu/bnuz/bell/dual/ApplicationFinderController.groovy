package cn.edu.bnuz.bell.dual

import org.springframework.security.access.prepost.PreAuthorize

@PreAuthorize('hasAuthority("PERM_DUALDEGREE_ADMIN")')
class ApplicationFinderController {
    ApplicationFinderService applicationFinderService

    def index() {
        String q = params.q
        renderJson applicationFinderService.find(q)
    }

    def show(Long id) {
        renderJson applicationFinderService.getFormForReview(id)
    }
}
