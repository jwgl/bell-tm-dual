package cn.edu.bnuz.bell.dualdegree

import org.springframework.security.access.prepost.PreAuthorize

@PreAuthorize('hasAuthority("PERM_DUALDEGREE_AGREEMENT_WRITE")')
class AgreementFormController {

    def index() { }
}
