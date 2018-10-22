package cn.edu.bnuz.bell.dual

import cn.edu.bnuz.bell.http.BadRequestException
import cn.edu.bnuz.bell.organization.Department
import cn.edu.bnuz.bell.report.ReportClientService
import cn.edu.bnuz.bell.report.ReportRequest
import org.springframework.security.access.prepost.PreAuthorize

@PreAuthorize('hasAuthority("PERM_DUALDEGREE_ADMIN")')
class ApplicationFinderController {
    ApplicationFinderService applicationFinderService
    ReportClientService reportClientService

    def index() {
        String q = params.q
        if (q ==~ /[0-9]{4}/) {
            renderJson applicationFinderService.findFinishedByYear(Integer.parseInt(q))
        } else {
            renderJson applicationFinderService.find(q)
        }
    }

    def show(Long id) {
        renderJson applicationFinderService.getFormForReview(id)
    }

    def export(String q, String type) {
        def reportName = "dual-${type}"
        def parameters

        if (type == 'applications-admin') {
            if (q ==~ /[0-9]{4}/) {
                def year = Integer.parseInt(q)
                // 如果参数是年份，表示要导出这年复学的名单
                reportName = "dual-finished-admin"
                parameters = [query: year]
            } else {
                // 如果参数是学院名称，birt暂时不支持中文传参，这里转为id
                def department = Department.findByName(q)
                parameters = [query: department ? department.id : q]
            }
        } else {
            throw new BadRequestException()
        }
        def reportRequest = new ReportRequest(
                reportName: reportName,
                parameters: parameters
        )
        reportClientService.runAndRender(reportRequest, response)
    }
}
