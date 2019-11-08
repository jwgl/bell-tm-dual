package cn.edu.bnuz.bell.dual

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
    ba.id as id,
    ba.title as title,
    ba.requestBegin as requestBegin,
    ba.requestEnd as requestEnd,
    (case when ba.paperEnd > ba.requestEnd then ba.paperEnd else null end) as paperEnd,
    (case when ba.approvalEnd > ba.requestEnd then ba.approvalEnd else null end) as approvalEnd,
    ba.creator as creator,
    ba.dateCreated as dateCreated,
    ba.department.name as departmentName
)
from Award ba, DepartmentAdministrator da 
join da.teacher t
where ba.department = da.department and da.teacher.id = :userId
order by ba.dateCreated desc
''', [userId: securityService.userId]
    }

    def list(String departmentId) {
        Award.executeQuery'''
select new map(
    ba.id as id,
    ba.title as title,
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
                applicationOn: false,
                paperOn: false,
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
from DepartmentAdministrator da 
join da.department d 
where da.teacher.id = :userId
''', [userId: securityService.userId]
    }

    /**
     * 查看详情
     */
    Map getFormForShow(Long id) {
        def results =Award.executeQuery '''
select new map(
    award.id as id,
    award.title as title,
    award.requestBegin as requestBegin,
    award.requestEnd as requestEnd,
    award.paperEnd as paperEnd,
    award.approvalEnd as approvalEnd,
    award.content as content,
    award.applicationOn as applicationOn,
    award.paperOn as paperOn,
    d.id as departmentId,
    d.name as departmentName
)
from Award award 
join award.department d
where award.id = :id
''', [id: id]
        if(!results) {
            throw new NotFoundException()
        }
        // 超级开关先只给特定学院开放
        def form  = results[0]
        form['toggleShow'] = form.departmentId == '20'
        return form
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

    def toggle(Long id, String key) {
        Award form = Award.load(id)
        form.setProperty(key, !form.getProperty(key))
        form.save(flush: true)
    }
}
