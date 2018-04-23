package cn.edu.bnuz.bell.dualdegree

import org.springframework.security.access.prepost.PreAuthorize

/**
 * 协议管理
 */
@PreAuthorize('hasAuthority("PERM_DUALDEGREE_AGREEMENT_WRITE")')
class AgreementController {
	AgreementService agreementService

    def index() {
        def cmd = new AgreementFilterCommand()
        bindData(cmd, params)
        renderJson(agreementService.list(cmd))
    }

    /**
     * 编辑数据
     */
    def edit(Long id) {
        renderJson([
                form: agreementService.getFormForEdit(id),
                regions: agreementService.regions,
                majors: agreementService.majors])
    }

    def show(Long id) {
        renderJson(agreementService.getFormForShow(id))
    }

    /**
     * 保存数据
     */
    def save() {
        def cmd = new AgreementCommand()
        bindData(cmd, request.JSON)
        def form = agreementService.create(cmd)
        renderJson([id: form.id])
    }

    /**
     * 更新数据
     */
    def update(Long id) {
        def cmd = new AgreementCommand()
        bindData(cmd, request.JSON)
        cmd.id = id
        agreementService.update(cmd)
        renderOk()
    }

    /**
     * 创建
     */
    def create() {
        renderJson([
                form: [
                        items: [],
                ],
                regions: agreementService.regions,
                majors: agreementService.majors
        ])
    }
}
