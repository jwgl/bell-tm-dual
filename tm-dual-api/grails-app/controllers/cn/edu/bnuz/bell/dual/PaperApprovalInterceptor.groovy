package cn.edu.bnuz.bell.dual

import cn.edu.bnuz.bell.security.SecurityService
import org.springframework.http.HttpStatus

class PaperApprovalInterceptor {
    SecurityService securityService

    boolean before() {
        if (params.mentorId != securityService.userId) {
            render(status: HttpStatus.FORBIDDEN)
            return false
        } else {
            return true
        }
    }

    boolean after() { true }

    void afterView() {}
}
