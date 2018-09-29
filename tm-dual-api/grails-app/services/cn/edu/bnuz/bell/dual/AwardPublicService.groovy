package cn.edu.bnuz.bell.dual

import cn.edu.bnuz.bell.http.BadRequestException
import cn.edu.bnuz.bell.security.SecurityService
import grails.gorm.transactions.Transactional

/**
 * 2018-09-29
 * 要求对学生显示的截至日期进行特殊处理
 */

@Transactional
class AwardPublicService {
    AwardService awardService
    SecurityService securityService

    def getAwardInfo(Long id) {
        def form = awardService.getFormForShow(id)
        if (form.departmentId != securityService.departmentId) {
            throw new BadRequestException()
        }
        // 部分学院不能在发布申请通知时确定论文提交时间和论文审核截至日期
        // 但这两个属性是非空属性，所以在这里只做屏蔽
        if (form.paperEnd <= form.requestEnd) {
            form.paperEnd = null
        }
        if (form.approvalEnd <= form.requestEnd) {
            form.approvalEnd = null
        }
        return form
    }
}
