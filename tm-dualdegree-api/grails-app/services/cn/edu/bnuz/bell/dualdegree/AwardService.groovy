package cn.edu.bnuz.bell.dualdegree

import cn.edu.bnuz.bell.http.NotFoundException
import cn.edu.bnuz.bell.organization.Department
import cn.edu.bnuz.bell.organization.Teacher
import cn.edu.bnuz.bell.security.SecurityService
import grails.gorm.transactions.Transactional

@Transactional
class AwardService {
    SecurityService securityService

    def list() {
        Award.executeQuery'''
select new map(
    ba.id   as id,
    ba.title   as title,
    ba.requestBegin as requestBegin,
    ba.requestEnd as requestEnd,
    ba.paperEnd as paperEnd,
    ba.approvalEnd as approvalEnd,
    ba.creator as creator,
    ba.dateCreated as dateCreated,
    ba.department.name as departmentName
)
from Award ba, DepartmentAdministrator da join da.teacher t
where ba.department = da.department and da.teacher.id = :userId
order by ba.dateCreated desc
''', [userId: securityService.userId]
    }

    def list(String departmentId) {
        Award.executeQuery'''
select new map(
    ba.id   as id,
    ba.title   as title,
    ba.requestBegin as requestBegin,
    ba.requestEnd as requestEnd,
    ba.paperEnd as paperEnd,
    ba.approvalEnd as approvalEnd,
    ba.creator as creator,
    ba.dateCreated as dateCreated,
    ba.department.name as departmentName
)
from Award ba
where ba.department.id = :departmentId
order by ba.dateCreated desc
''', [departmentId: departmentId]
    }

    /**
     * 保存
     */
    def create(AwardCommand cmd) {
        Award form = new Award(
                title: cmd.title,
                content: cmd.content,
                requestBegin: cmd.requestBeginToDate,
                requestEnd: cmd.requestEndToDate,
                paperEnd: cmd.paperEndToDate,
                approvalEnd: cmd.approvalEndToDate,
                creator: Teacher.load(securityService.userId),
                dateCreated: new Date(),
                department: Department.load(cmd.departmentId)
        )

        form.save()
        return form
    }

    /**
     * 所管理的学院
     */
    def getMyDepartments () {
        DepartmentAdministrator.executeQuery'''
select new map(
    d.id as id,
    d.name as name
)
from DepartmentAdministrator da join da.department d 
where da.teacher.id = :userId
''',[userId: securityService.userId]
    }

    /**
     * 查看详情
     */
    Map getFormForShow(Long id) {
        def results =Award.executeQuery '''
select new map(
    award.id           as      id,
    award.title        as      title,
    award.requestBegin as      requestBegin,
    award.requestEnd   as      requestEnd,
    award.paperEnd     as      paperEnd,
    award.approvalEnd  as      approvalEnd,
    award.content      as      content,
    d.id               as      departmentId,
    d.name             as      departmentName
    
)
from Award award join award.department d
where award.id = :id
''',[id: id]
        if(!results) {
            throw new NotFoundException()
        }
        return results[0]
    }

    /**
     * 更新
     */
    def update(AwardCommand cmd) {
        Award form = Award.load(cmd.id)
        if (form) {
            form.title = cmd.title
            form.content = cmd.content
            form.requestBegin = cmd.requestBeginToDate
            form.requestEnd = cmd.requestEndToDate
            form.paperEnd = cmd.paperEndToDate
            form.approvalEnd = cmd.approvalEndToDate

            form.save(flush: true)
            return form
        }
    }

    def getMessage() {
        return [title: '英国留学项目']
    }
}
