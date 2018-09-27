package cn.edu.bnuz.bell.dualdegree

import cn.edu.bnuz.bell.security.SecurityService
import org.springframework.http.HttpStatus

class ApplicationAdministrateInterceptor {
    SecurityService securityService

    boolean before() {
        if (params.departmentId != securityService.departmentId) {
            render(status: HttpStatus.FORBIDDEN)
            return false
        } else {
            return true
        }
    }

    boolean after() { true }

    void afterView() {}
}
