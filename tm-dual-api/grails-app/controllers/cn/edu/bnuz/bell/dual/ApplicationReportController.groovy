package cn.edu.bnuz.bell.dual

import cn.edu.bnuz.bell.http.BadRequestException
import cn.edu.bnuz.bell.report.ReportClientService
import cn.edu.bnuz.bell.report.ReportRequest
import cn.edu.bnuz.bell.security.SecurityService
import org.springframework.security.access.prepost.PreAuthorize

@PreAuthorize('hasAuthority("PERM_DUALDEGREE_DEPT_ADMIN")')
class ApplicationReportController {
    ReportClientService reportClientService
    SecurityService securityService

    def show(Integer awardId, Integer applicationId, String type) {
        def reportName = "dual-${type}"
        def parameters
        def format
        switch (type) {
            case 'paper-list':
            case 'application-list':
            case 'paper-audit':
                format = 'xlsx'
                parameters = [department_id: securityService.departmentId, award_id: awardId]
                break
            case 'paper-approval':
                parameters = [department_id: securityService.departmentId, award_id: awardId, myid: applicationId]
                format = 'pdf'
                break
            case 'paper-approval-all':
            case 'pass-list':
                parameters = [department_id: securityService.departmentId, award_id: awardId]
                format = 'pdf'
                break
            default:
                throw new BadRequestException()
        }
        def reportRequest = new ReportRequest(
                reportName: reportName,
                format: format,
                parameters: parameters
        )
        reportClientService.runAndRender(reportRequest, response)
    }
}
