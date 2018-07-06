package cn.edu.bnuz.bell.dualdegree

import cn.edu.bnuz.bell.security.SecurityService
import org.springframework.security.access.prepost.PreAuthorize

/**
 * 协议查看--教务处
 */
@PreAuthorize('hasAuthority("PERM_DUALDEGREE_AGREEMENT_READ")')
class AgreementPublicController {
    AgreementService agreementService
    SecurityService securityService

    def index() {
        def cmd = new AgreementFilterCommand()
        bindData(cmd, params)
        renderJson(agreementService.list(cmd))
    }

    def show(Long id) {
        renderJson(agreementService.getFormForShow(id))
    }

    /**
     * 学院协议查看
     */
    @PreAuthorize('hasAuthority("PERM_DUALDEGREE_DEPT_ADMIN")')
    def agreementsOfDept() {
        def agreements = agreementService.findAgreementsByDepartment(securityService.departmentId)
        renderJson(agreements)
    }
}
