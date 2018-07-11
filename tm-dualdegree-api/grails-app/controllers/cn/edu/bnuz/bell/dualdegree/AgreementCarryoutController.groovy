package cn.edu.bnuz.bell.dualdegree

import org.springframework.security.access.prepost.PreAuthorize

/**
 * 协议执行
 */
@PreAuthorize('hasAuthority("PERM_DUALDEGREE_AGREEMENT_WRITE")')
class AgreementCarryoutController {
    AgreementCarryoutService agreementCarryoutService
    def index() {
        renderJson(agreementCarryoutService.list())
    }

    def save() {
        def cmd = new AgreementCarryoutCommand()
        bindData(cmd, request.JSON)
        def form = agreementCarryoutService.create(cmd)
        renderJson([id: form.id])
    }
}
