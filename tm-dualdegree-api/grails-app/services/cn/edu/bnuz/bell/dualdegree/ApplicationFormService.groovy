package cn.edu.bnuz.bell.dualdegree

import cn.edu.bnuz.bell.http.BadRequestException
import cn.edu.bnuz.bell.http.ForbiddenException
import cn.edu.bnuz.bell.http.NotFoundException
import cn.edu.bnuz.bell.organization.Department
import cn.edu.bnuz.bell.organization.Student
import cn.edu.bnuz.bell.organization.Teacher
import cn.edu.bnuz.bell.security.SecurityService
import cn.edu.bnuz.bell.workflow.DomainStateMachineHandler
import cn.edu.bnuz.bell.workflow.commands.SubmitCommand
import grails.gorm.transactions.Transactional
import org.springframework.beans.factory.annotation.Value

import java.time.LocalDate

@Transactional
class ApplicationFormService {
    @Value('${bell.student.filesPath}')
    String filesPath
    DomainStateMachineHandler domainStateMachineHandler
    SecurityService securityService

    /**
     * @param userId 申请人Id
     * @return 已申请过的授予通知和申请状态、正在开放的授予通知
     */
    def list(String userId) {

        DegreeApplication.executeQuery'''
select new map(
    da.id as applicationId,
    ba.id   as id,
    ba.title   as title,
    ba.requestBegin as requestBegin,
    ba.requestEnd as requestEnd,
    ba.paperEnd as paperEnd,
    ba.approvalEnd as approvalEnd,
    ba.creator as creator,
    ba.dateCreated as dateCreated,
    ba.department.name as departmentName,
    da.status as status
)
from DegreeApplication da 
right join da.award ba with da.student.id = :userId
where ba.approvalEnd >= :date and ba.department.id = :departmentId
''', [userId: userId, date: LocalDate.now(),departmentId: securityService.departmentId]
    }

    /**
     * 保存本人在指定学位授予批次的申请单
     * @param userId 申请人ID
     * @return 持久化的表单
     */
    def create(String userId, ApplicationFormCommand cmd) {
        def award = Award.get(cmd.awardId)
        def student = Student.load(userId)
        if (award && student) {
            if (!award.betweenApplyDateRange() ||
                DegreeApplication.findByStudentAndAward(student, award)) {
                //同一批每人只能一个申请， 超期禁止申请
                throw new ForbiddenException()
            }
        } else {
            // 非法访问
            throw new BadRequestException()
        }
        def now = new Date()

        DegreeApplication form = new DegreeApplication(
                award: award,
                student: student,
                approver: award.creator,
                dateCreated: LocalDate.now(),
                universityCooperative: cmd.universityCooperative,
                majorCooperative: cmd.majorCooperative,
                email: cmd.email,
                linkman: cmd.linkman,
                phone: cmd.phone,
                dateModified: now,
                status: domainStateMachineHandler.initialState
        )

        if (!form.save()) {
            form.errors.each {
                println it
            }
        }
        domainStateMachineHandler.create(form, userId)
        return form
    }

    /**
     * 保存本人在指定学位授予批次的申请单
     * @param userId 申请人ID
     * @return 持久化的表单
     */
    def update(String userId, ApplicationFormCommand cmd) {
        DegreeApplication form = DegreeApplication.load(cmd.id)
        Award award = Award.get(cmd.awardId)
        if (form.award != award || form.student.id != userId) {
            //无权更新
            throw new ForbiddenException()
        }
        def now = new Date()

        form.setUniversityCooperative(cmd.universityCooperative)
        form.setMajorCooperative(cmd.majorCooperative)
        form.setEmail(cmd.email)
        form.setLinkman(cmd.linkman)
        form.setPhone(cmd.phone)
        form.setDateModified(now)

        if (!form.save()) {
            form.errors.each {
                println it
            }
        }
        return form
    }

    /**
     * 获取本人在指定学位授予批次的申请单用于显示
     * @param userId 申请人ID
     * @param id 申请单ID
     * @return 申请信息
     */
    Map getFormInfo(Long id) {
        def results = DegreeApplication.executeQuery'''
select new map(
  form.id as id,
  award.id as awardId,
  student.id as studentId,
  student.name as studentName,
  form.phone as phone,
  form.linkman as linkman,
  form.email as email,
  form.universityCooperative as universityCooperative,
  form.majorCooperative as majorCooperative,
  form.dateCreated as dateCreated,
  form.dateModified as dateModified,
  form.dateSubmitted as dateSubmitted,
  approver.id as approverId,
  approver.name as approver,
  paperApprover.id as paperApproverId,
  paperApprover.name as paperApprover,
  form.dateApproved as dateApproved,
  form.status as status,
  form.workflowInstance.id as workflowInstanceId
)
from DegreeApplication form
join form.award award
join form.student student
left join form.approver approver
left join form.paperApprover paperApprover
where form.id = :id
''', [id: id]
        if (!results) {
            return null
        }
        def form = results[0]
        if (form.paperApproverId) {
            form['mentorEmail'] = Mentor.findByTeacherAndDepartment(
                    Teacher.load(form.paperApproverId),
                    Department.load(securityService.departmentId))?.email
        }

        return form
    }

    /**
     * 获取申请单信息，用于显示
     * @param userId 申请人ID
     * @param id 申请单ID
     * @return 申请信息
     */
    Map getFormForShow (String userId, Long id) {
        def form = getFormInfo(id)
        if (!form) {
            throw new NotFoundException()
        }
        if (form.studentId != userId) {
            throw new ForbiddenException()
        }
        return form
    }

    Map getFormForCreate(String userId, Long awardId) {
        Award award = Award.get(awardId)
        Student student = Student.get(userId)
        //15级是分水岭，以前的采用CooperativeUniversity，后面的采用协议中的合作大学
        def universities
        if (student.major.grade <= 2015) {
            universities = getCooperativeUniversity(student.department.id)
        } else {
            universities = getCooperativeUniversity(student)
        }
        return [
                form: [],
                timeNode: [
                        requestBegin: award.requestBegin,
                        requestEnd: award.requestEnd,
                        paperEnd: award.paperEnd,
                        approvalEnd: award.approvalEnd
                ],
                universities: universities,
                fileNames: findFiles(userId, awardId)
        ]
    }

    def getFormForEdit(String userId, Long id) {
        def application = DegreeApplication.get(id)
        if (!application) {
            throw new BadRequestException()
        }
        Map vm = getFormForCreate(userId, application.awardId)
        vm.form = getFormForShow(userId, id)

        return vm
    }

    def submit(String userId, SubmitCommand cmd) {
        DegreeApplication form = DegreeApplication.get(cmd.id)

        if (!form) {
            throw new NotFoundException()
        }

        if (form.student.id != userId) {
            throw new ForbiddenException()
        }

        if (!domainStateMachineHandler.canSubmit(form)) {
            throw new BadRequestException()
        }
        domainStateMachineHandler.submit(form, userId, cmd.to, cmd.comment, cmd.title)

        form.dateSubmitted = new Date()
        form.save()
    }

    private List<String> getCooperativeUniversity(String departmentId) {
        CooperativeUniversity.executeQuery'''
select new map(c.name as universityEn)
from CooperativeUniversity c
where c.department.id = :departmentId
''', [departmentId: departmentId]
    }

    private List<Map<String, String>> getCooperativeUniversity(Student student) {
        AgreementMajor.executeQuery'''
select new map(
    ag.universityEn as universityEn,
    ag.universityCn as universityCn
)
from AgreementMajor agmj 
join agmj.agreement ag
join ag.region agRegion,
StudentAbroad sa join sa.agreementRegion saRegion join sa.student student
where agRegion = saRegion and student.id = :studentId and student.major = agmj.major
''', [studentId: student.id]
    }

    Map<String, String> findFiles(String studentId, id) {
        File dir = new File("${filesPath}/${id}/${studentId}")
        if (!dir.exists()) {
            return [:]
        }
        Map<String, Object> fileNames = [:]
        for (File file: dir.listFiles()) {
            def index = file.name.indexOf('_')
            if (index == -1) {
                continue
            }
            String key = file.name.substring(0, index)
            if (key == 'bak') {
                key = file.name.substring(0,file.name.indexOf('_', 4))
                if (key == 'bak_paper' || key == 'bak_review') {
                    fileNames[key] = fileNames[key] ? fileNames[key] + [file.name] : [file.name]
                }
            } else {
                fileNames[key] = file.name
            }

        }
        return fileNames
    }

    def getAward(Long awardId) {
        Award.get(awardId)
    }

    def getUser(Long id) {
        DegreeApplication.executeQuery'''
select new map(s.id as id, s.name as name)
from DegreeApplication da 
join da.student s
where da.id = :id
''', [id: id]
    }
}
