package cn.edu.bnuz.bell.dualdegree

import org.springframework.security.access.prepost.PreAuthorize

@PreAuthorize('hasAuthority("PERM_DUALDEGREE_PAPER_APPROVE")')
class PaperApprovalController {

    def index() { }
}
