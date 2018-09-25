package cn.edu.bnuz.bell.dual

import cn.edu.bnuz.bell.organization.Department
import cn.edu.bnuz.bell.organization.Teacher
import cn.edu.bnuz.bell.security.SecurityService
import grails.gorm.transactions.Transactional

@Transactional
class MentorService {
    SecurityService securityService

    /**
     * 导师列表
     * @return
     */
    def list() {
        Mentor.executeQuery'''
select new map(
    m.id as id,
    t.id as teacherId,
    t.name as teacherName,
    t.sex as sex,
    t.academicTitle as academicTitle,
    t.academicDegree as academicDegree,
    m.email as email,
    d.name as departmentName
)
from Mentor m 
join m.teacher t 
join m.department d
where d.id = :department
''', [department: securityService.departmentId]
    }

    /**
     * 保存
     * @param cmd
     * @return
     */
    def create(MentorCommand cmd) {
        Mentor mentor = new Mentor(
                teacher: Teacher.load(cmd.teacherId),
                email: cmd.email,
                department: Department.load(securityService.departmentId)
        )
        mentor.save()
        return mentor
    }

    /**
     * 编辑
     * @param id
     * @return
     */
    def getFormForEdit(Long id) {
        Mentor.executeQuery'''
select new map(
    m.id as id,
    t.id as teacherId,
    t.name as teacherName,
    m.email as email
)
from Mentor m 
join m.teacher t
where m.id = :id
''', [id: id]
    }

    /**
     * 删除
     * @param id
     * @return
     */
    def delete(Long id) {
        def form = Mentor.get(id)
        if (form) {
            form.delete()
        }
    }

    /**
     * 更新Email
     */
    def update(Long id, MentorCommand cmd) {
        Mentor mentor = Mentor.get(id)
        mentor.setEmail(cmd.email)
        mentor.save()
    }

    def getPaperProvers() {
        Mentor.executeQuery'''
select new map(t.id as id, t.name as name)
from Mentor m 
join m.teacher t 
where d.department.id = :department
''', [department: securityService.departmentId]
    }
}
