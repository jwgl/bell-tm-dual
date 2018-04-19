package cn.edu.bnuz.bell.dualdegree

import cn.edu.bnuz.bell.dualdegree.eto.StudentAbroadEto
import cn.edu.bnuz.bell.master.Major
import cn.edu.bnuz.bell.organization.Teacher
import cn.edu.bnuz.bell.security.SecurityService
import cn.edu.bnuz.bell.security.UserLogService
import grails.converters.JSON
import grails.gorm.transactions.Transactional

@Transactional
class StudentAbroadService {
    SecurityService securityService
    StudentValidateService studentValidateService
    UserLogService userLogService

    /**
     * 出国学生列表
     */
    def list(StudentOptionsCommand cmd) {
        def result = StudentAbroad.executeQuery'''
select new map(
sa.id as id,
g.name as regionName,
g.id as regionId,
st.id as studentId,
st.name as studentName,
st.sex as sex,
st.atSchool as atSchool,
d.name as departmentName,
mj.grade as grade,
sj.name as subjectName,
sj.id as subjectId,
ac.name as adminClassName
)
from StudentAbroad sa 
join sa.student st 
join sa.agreementRegion g
join st.department d 
join st.major mj 
join mj.subject sj 
join st.adminClass ac
where d.id in (:departments) and sa.enabled is true
''',[departments: studentValidateService.deptAdmins]
        return result.grep{
            (cmd.sujectId ? cmd.sujectId == it.subjectId : true) &&
            (cmd.grade ? cmd.grade == it.grade : true) &&
            (cmd.regionId ? cmd.regionId == it.regionId : true) &&
            (cmd.studentName ? cmd.studentName == it.studentName : true) &&
            (cmd.studentId ? cmd.studentId == it.studentId : true)
        }
    }

    /**
     * 保存
     */
    def create(StudentAbroadCommand cmd) {
        def validate = studentValidateService.validate(cmd)
        if (validate.error) {
            return validate.error
        }
        def students = validate.students
        def studentsEto = validate.studentsEto
        def me = Teacher.load(securityService.userId)
        def region = AgreementRegion.load(cmd.regionId)
        StudentAbroad.executeUpdate'''
    insert into StudentAbroad (student, operator, dateCreated, agreementRegion, enabled)
    select st, :user, now(), :agreementRegion, true from Student st where st.id in (:ids) 
''',[user: me, agreementRegion: region, ids: students]
//      写入自助打印系统
        if (studentsEto && studentsEto.size()) {
            StudentAbroadEto.executeUpdate'''
    insert into StudentAbroadEto (studentId, studentName, dateCreated, creator, enabled, region)
    select st.id, st.name, now(), :userId, true, :agreementRegion from Student st where st.id in (:ids)
''',[userId: me.id, agreementRegion: region.name, ids: studentsEto]
        }
        userLogService.log(securityService.userId,securityService.ipAddress,"CREATE", students.size(),"批量导入出国学生")
        return null
    }

    /**
     * 删除
     * @param id
     * @return
     */
    def delete(Long id) {
        def form = StudentAbroad.get(id)
        if (form && form.student.department.id == securityService.departmentId) {
            form.delete()
            userLogService.log(securityService.userId,securityService.ipAddress,"DELETE", form, "${form as JSON}")
            // 同步删除自助打印系统上名单
            def studentEto = StudentAbroadEto.findByStudentId(form.student.id)
            if (studentEto) {
                studentEto.delete()
            }
        }
    }

    def getAgreementRegions() {
        AgreementRegion.executeQuery'''
select new map(g.id as id, g.name as name) 
from AgreementRegion g
'''
    }

    def getSubjects() {
        Major.executeQuery'''
select distinct new map(
sj.id as id,
sj.name as name
)
from Major mj join mj.subject sj
where mj.department.id in (:departments) and sj.isDualDegree is true
order by sj.name
''',[departments: studentValidateService.deptAdmins]
    }

    def getGrades() {
        Major.executeQuery'''
select distinct mj.grade
from Major mj join mj.subject sj
where mj.department.id in (:departments) and sj.isDualDegree is true
order by mj.grade
''',[departments: studentValidateService.deptAdmins]
    }
}
