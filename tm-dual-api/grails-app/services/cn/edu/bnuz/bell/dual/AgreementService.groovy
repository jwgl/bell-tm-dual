package cn.edu.bnuz.bell.dual

import cn.edu.bnuz.bell.http.BadRequestException
import cn.edu.bnuz.bell.master.Subject
import cn.edu.bnuz.bell.security.SecurityService
import cn.edu.bnuz.bell.security.UserLogService
import grails.converters.JSON
import grails.gorm.transactions.Transactional


@Transactional
class AgreementService {
    SecurityService securityService
    UserLogService userLogService

    /**
     * 协议列表
     */
    def list(AgreementFilterCommand cmd) {
        def queryStr = '''
select distinct new map(
    agreement.id as id,
    agreement.name as name,
    gr.name as regionName,
    u.nameEn as universityEn,
    u.nameCn as universityCn,
    agreement.memo as memo
)
from Agreement agreement 
join agreement.university u
join u.region gr
join agreement.item item
join item.subject subject
join subject.department department
where agreement.name like :name
and gr.name like :regionName
and department.name like :departmentName
and subject.name like :subjectName
and :grade between item.startedGrade and item.endedGrade
and u.nameCn like :universityCn
order by agreement.name
'''
        if (!cmd.grade) queryStr = queryStr.replace('between item.startedGrade and item.endedGrade', '= 1')
        def results = Agreement.executeQuery queryStr,
                [name: cmd.name ? "%${cmd.name}%" : '%',
                 regionName: cmd.regionName ?: '%',
                 departmentName: cmd.department ?: '%',
                 grade: cmd.grade ?: 1,
                 subjectName: cmd.subjectName ?: '%',
                 universityCn: cmd.universityCn ? "%${cmd.name}%" : '%']
        return [list: results, majors: this.subjects, regions: this.regions]
    }

    /**
     * 保存协议
     */
    def create(AgreementCommand cmd) {
        Agreement form = new Agreement(
                name: cmd.agreementName,
                university: CooperativeUniversity.load(cmd.universityId),
                memo: cmd.memo
        )
        cmd.addedItems.each { item ->
            def agreementMajor = new AgreementSubject(
                    subject: Subject.load(item.id),
                    startedGrade: item.startedGrade,
                    endedGrade: item.endedGrade,
                    dateCreated: new Date()
            )
            item.coMajors.each { id ->
                agreementMajor.addToItems(new AgreementCooperativeMajor(
                        cooperativeMajor: CooperativeMajor.load(id)
                ))
            }
            form.addToItem(agreementMajor)
        }
        form.save(flush: true)
        return form
    }

    /**
     * 区域列表
     */
    def getRegions() {
        AgreementRegion.executeQuery'''
select new map(
    gr.id as id,
    gr.name as name
)
from AgreementRegion gr
'''
    }

    /**
     * 2+2专业列表
     */
    def getSubjects() {
        Subject.executeQuery'''
select new map(
    subject.id as id,
    subject.name as subjectName,
    department.id as departmentId,
    department.enabled as enabled,
    department.name as departmentName    
)
from Subject subject 
join subject.department department
where subject.isDualDegree is true
order by department.name, subject.name
'''
    }

    /**
     * 编辑
     */
    def getFormForEdit(Long id) {
        Agreement form = Agreement.get(id)
        if (!form) {
            throw new BadRequestException()
        }
        def items = findAgreementSubjects(id)
        items.each { item ->
            item['coMajors'] = findCoMajors(item.agreementSubjectId as Long)
        }
        return [
                id: form.id,
                agreementName: form.name,
                memo: form.memo,
                items: items,
                university: findAgreementUniversity(id)
        ]
    }

    /**
     * 浏览
     */
    def getFormForShow(Long id) {
        def result = Agreement.executeQuery '''
select new map(
    agreement.id as id,
    agreement.name as agreementName,
    agreement.memo as memo
)
from Agreement agreement
where agreement.id = :id
''', [id: id]
        if (result) {
            def form = result[0]
            def items = findAgreementSubjects(id)
            items.each { item ->
                item['coMajors'] = findCoMajors(item.agreementSubjectId as Long)
            }
            form['items'] = items
            form['university'] = findAgreementUniversity(id)
            return form
        } else {
            return []
        }
    }

    /**
     * 更新
     */
    def update(AgreementCommand cmd) {
        Agreement form = Agreement.load(cmd.id)
        if (form) {
            // 更新前先做日志
            userLogService.log(securityService.userId,securityService.ipAddress,"UPDATE", form,"${form as JSON}")
            form.name = cmd.agreementName
            form.university = CooperativeUniversity.load(cmd.universityId)
            form.memo = cmd.memo

            def oldItems = AgreementSubject.findAllByAgreement(form)
            oldItems.each {
                it.delete()
                form.removeFromItem(it)
            }
            form.save(flush: true)
            cmd.addedItems.each { item ->
                def agreementMajor = new AgreementSubject(
                        subject: Subject.load(item.id),
                        startedGrade: item.startedGrade,
                        endedGrade: item.endedGrade,
                        dateCreated: new Date()
                )
                item.coMajors.each { id ->
                    agreementMajor.addToItems(new AgreementCooperativeMajor(
                            cooperativeMajor: CooperativeMajor.load(id)
                    ))
                }
                form.addToItem(agreementMajor)
            }
            form.save(flush: true)
            return form
        }
    }

    def findAgreementSubjects(Long agreementId) {
        AgreementSubject.executeQuery'''
select new map(
    am.id as agreementSubjectId,
    subject.id as id,
    subject.name as subjectName,
    am.startedGrade as startedGrade,
    am.endedGrade as endedGrade,
    department.id as departmentId,
    department.name as departmentName
)
from AgreementSubject am 
join am.subject subject 
join subject.department department
where am.agreement.id = :id
order by department.name, subject.name
''', [id: agreementId]
    }

    def findCoMajors(Long itemId) {
        AgreementCooperativeMajor.executeQuery'''
select new map(
    cm.id as id,
    cm.nameEn as nameEn,
    cm.nameCn as nameCn,
    cm.bachelor as bachelor    
)
from AgreementCooperativeMajor acm 
join acm.cooperativeMajor cm
where acm.agreementMajor.id = :id
''', [id: itemId]
    }

    def findAgreementUniversity(Long id) {
        def result = Agreement.executeQuery '''
select new map(
    c.id as id,
    c.nameEn as nameEn,
    c.shortName as shortName,
    c.nameCn as nameCn,
    r.name as region
)
from Agreement a 
join a.university c 
join c.region r
where a.id = :id
''', [id: id]
        if (result) {
            return result[0]
        } else {
            return null
        }
    }

    /**
     * 特定学院相关协议
     */
    def findAgreementsByDepartment(String departmentId) {
        def list = AgreementSubject.executeQuery'''
select distinct new map(
    item.id as id,
    item.startedGrade as startedGrade,
    item.endedGrade as endedGrade,
    sj.name as subjectName,
    gr.name as regionName,
    u.nameEn as nameEn
)
from AgreementSubject item 
join item.subject sj 
join sj.department d
join item.agreement ag
join ag.university u
join u.region gr
where d.id = :id
''', [id: departmentId]
        list.each { item ->
            item['coMajors'] = findCoMajors(item.id as Long)
        }
        return list
    }

    /**
     * 合作大学
     */
    def getUniversities() {
        CooperativeUniversity.executeQuery '''
select new map(
    c.id as id,
    c.nameEn as nameEn,
    c.shortName as shortName,
    c.nameCn as nameCn,
    r.name as region
)
from CooperativeUniversity c 
join c.region r
'''
    }
}
