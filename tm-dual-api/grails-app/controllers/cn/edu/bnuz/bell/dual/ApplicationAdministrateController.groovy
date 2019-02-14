package cn.edu.bnuz.bell.dual

import cn.edu.bnuz.bell.http.BadRequestException
import cn.edu.bnuz.bell.utils.ZipTools
import org.springframework.beans.factory.annotation.Value
import org.springframework.security.access.prepost.PreAuthorize


@PreAuthorize('hasAuthority("PERM_DUALDEGREE_DEPT_ADMIN")')
class ApplicationAdministrateController {
    ApplicationAdministrateService applicationAdministrateService
    @Value('${bell.student.filesPath}')
    String filesPath

    def index(String departmentId, Long awardId, String status) {
        def statusList = applicationAdministrateService.statusList(departmentId, awardId)
        if (status != 'undefined') {
            renderJson([
                    statusList: statusList,
                    applicationList: applicationAdministrateService.list(departmentId, awardId, status)
                    ])
        } else {
            if (statusList && statusList.size()) {
                renderJson([
                        statusList: statusList,
                        applicationList: applicationAdministrateService.list(departmentId, awardId, statusList[0].status as String)
                ])
            }
        }

    }

    def show(String departmentId, Long awardId, Long id) {
        renderJson applicationAdministrateService.getFormForReview(departmentId, awardId, id)
    }

    def attachments(String departmentId, Long awardId, String status, String pre) {
        def students
        switch (status) {
            case 'STEP1':
            case 'STEP2':
            case 'STEP3':
            case 'STEP4':
            case 'STEP5':
            case 'FINISHED':
                students = applicationAdministrateService.findUsers(departmentId, awardId, status)
                break
            case 'ALL':
                students = applicationAdministrateService.findUsers(departmentId, awardId)
                break
            default:
                throw new BadRequestException()
        }
        def filename = "attachments_" + departmentId
        def basePath = "${filesPath}/${awardId}/"
        def zipTools = new ZipTools()
        response.setHeader("Content-disposition", "filename=\"${filename}.zip\"")
        response.contentType = "application/zip"
        response.outputStream << zipTools.zip(students, basePath, pre)
        response.outputStream.flush()
    }
}
