package cn.edu.bnuz.bell.dualdegree

import cn.edu.bnuz.bell.dualdegree.eto.StudentAbroadEto
import cn.edu.bnuz.bell.organization.Student
import cn.edu.bnuz.bell.security.SecurityService
import grails.gorm.transactions.Transactional

@Transactional
class StudentValidateService {
    SecurityService securityService

    def validate(StudentAbroadCommand cmd) {
        def error = new ArrayList<String>()
        def ids = cmd.studentIds.split("\n")
        def students = findStudents(ids)

        // 检查是否存在越权导入
        if (ids.length != students.size()) {
            def ids_invalid = (ids as String[]) - students
            error.add("非本学院或非2+2专业学生: ${ids_invalid.toArrayString()}")
            return [error: error]
        }

        // 检查是否存在重复导入
        def duplicates = hasDuplicates(ids)
        if (duplicates) {
            error.add("重复导入: ${duplicates.toListString()}")
            return [error: error]
        }

        // 检查学生参加的项目是否有协议支持
        def regionMatchIds = regionMatch(ids, cmd.regionId)
        if (ids.length != regionMatchIds.size()) {
            def ids_invalid = (ids as String[]) - regionMatchIds
            error.add("学生所在专业不能参加所选项目: ${ids_invalid.toArrayString()}")
            return [error: error]
        }

        // 去除已经导入成绩自助打印系统的学生
        def duplicatesInStudentEto = hasDuplicatesInStudentEto(ids)
        def studentsEto = students
        if (duplicatesInStudentEto) {
            studentsEto = students.grep{
                !(it in duplicatesInStudentEto)
            }
        }
        return [error: null, students: students, studentsEto: studentsEto]
    }
    def getDeptAdmins() {
        DepartmentAdministrator.executeQuery'''
select d.id from DepartmentAdministrator da join da.department d 
where da.teacher.id = :userId
''', [userId: securityService.userId]
    }

    def findStudents(String[] ids) {
        Student.executeQuery'''
select st.id
from Student st join st.department d join st.major mj join mj.subject sj
where d.id in (:departments) and st.id in (:ids) and sj.isDualDegree is true
''', [departments: deptAdmins, ids: ids]
    }

    def hasDuplicates(String[] ids) {
        StudentAbroad.executeQuery'''
select st.student.id
from StudentAbroad st 
where st.student.id in (:ids)
''', [ids: ids]
    }

    def regionMatch(String[] ids, Long regionId) {
        Student.executeQuery'''
select distinct st.id
from Student st join st.major major, 
Agreement agreement join agreement.item item join agreement.university university
where st.id in (:ids) and major.subject.id = item.subject.id 
and major.grade between item.startedGrade - 1 and item.endedGrade
and university.region.id = :regionId
''', [ids: ids, regionId: regionId]
    }

    def hasDuplicatesInStudentEto(String[] ids) {
        StudentAbroadEto.executeQuery'''
select st.studentId
from StudentAbroadEto st 
where st.studentId in (:ids)
''', [ids: ids]
    }
}
