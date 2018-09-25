package cn.edu.bnuz.bell.dual

import java.time.LocalDate
import java.time.format.DateTimeFormatter

class AwardCommand {
    Long id
    String title
    String content
    String requestBegin
    String requestEnd
    String paperEnd
    String approvalEnd
    String departmentId

    LocalDate toDate(String dateStr) {
        DateTimeFormatter spdf = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        if(dateStr!=null){
            LocalDate date=LocalDate.parse(dateStr, spdf)
            return date
        }else{
            return null
        }
    }

    def getRequestBeginToDate() {
        return toDate(requestBegin)
    }

    def getRequestEndToDate() {
        return toDate(requestEnd)
    }

    def getPaperEndToDate() {
        return toDate(paperEnd)
    }

    def getApprovalEndToDate() {
        return toDate(approvalEnd)
    }
}
