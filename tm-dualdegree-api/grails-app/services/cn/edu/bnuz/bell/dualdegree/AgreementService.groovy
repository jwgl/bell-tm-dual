package cn.edu.bnuz.bell.dualdegree

import cn.edu.bnuz.bell.dualdegree.dv.DvAgreementMajorsView
import cn.edu.bnuz.bell.dualdegree.eto.MajorRegionEto
import cn.edu.bnuz.bell.http.BadRequestException
import cn.edu.bnuz.bell.master.Subject
import cn.edu.bnuz.bell.security.SecurityService
import cn.edu.bnuz.bell.security.UserLogService
import cn.edu.bnuz.bell.utils.CollectionUtils
import cn.edu.bnuz.bell.utils.GroupCondition
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
    agreement.id                as      id,
    agreement.name              as      name,
    gr.name                     as      regionName,
    u.nameEn                    as      universityEn,
    u.nameCn                    as      universityCn,
    agreement.memo              as      memo
)
from Agreement agreement join agreement.university u
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
                name:               cmd.agreementName,
                university:         CooperativeUniversity.load(cmd.universityId),
                memo:               cmd.memo
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
            // 添加到自助打印系统中，改到执行时才推送
//            def majorRegionEto = new MajorRegionEto(majorId: item.id, region: form.university.region.name)
//            def check = MajorRegionEto.get(majorRegionEto)
//            if (!check) {
//                majorRegionEto.save()
//            }
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
    gr.id   as id,
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
    subject.id      as id,
    subject.name    as subjectName,
    department.id   as departmentId,
    department.enabled as enabled,
    department.name as departmentName    
)
from Subject subject join subject.department department
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
        def items = findAgreementMajors(id)
        items.each { item ->
            item['coMajors'] = findCoMajors(item.id as Long)
        }
        return [
                id: form.id,
                agreementName: form.name,
                memo: form.memo,
                items: items,
                university: [
                        id: form.university.id,
                        nameCn: form.university.nameCn,
                        region: form.university.region.name,
                        nameEn: form.university.nameEn
                ]
        ]
    }

    /**
     * 浏览
     */
    def getFormForShow(Long id) {
        def result = Agreement.executeQuery '''
select new map(
    agreement.id                as      id,
    agreement.name              as      agreementName,
    gr.name                     as      regionName,
    u.nameEn                    as      universityEn,
    u.nameCn                    as      universityCn,
    agreement.memo              as      memo
)
from Agreement agreement join agreement.university u
join u.region gr
where agreement.id = :id
''', [id: id]
        if (result) {
            def form = result[0]
            def items = findAgreementMajors(id)
            List<GroupCondition> conditions = [
                    new GroupCondition(
                            groupBy: 'departmentId',
                            into: 'subjects',
                            mappings: [
                                    departmentId  : 'id',
                                    departmentName: 'name'
                            ]
                    ),
                    new GroupCondition(
                            groupBy: 'subjectName',
                            into: 'options',
                            mappings: [
                                    subjectName: 'name'
                            ]
                    ),
                    new GroupCondition(
                            groupBy: 'majorOptions',
                            into: 'grades',
                            mappings: [
                                    majorOptions: 'name'
                            ]
                    ),
            ]
            form['items'] = CollectionUtils.groupBy(items, conditions)
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
            form.name = cmd.agreementName
            form.university = CooperativeUniversity.load(cmd.universityId)
            form.memo = cmd.memo

            cmd.addedItems.each { item ->
                def major = Major.load(item.id)
                def agreementItem = AgreementSubject.get(new AgreementSubject(agreement: form, subject: major))
                if (!agreementItem) {
                    form.addToItem(new AgreementSubject(
                            subject: major,
                            dateCreated: new Date()
                    ))
                }

                // 添加到自助打印系统中
                def majorRegionEto = new MajorRegionEto(majorId: item.id, region: form.region.name)
                def check = MajorRegionEto.get(majorRegionEto)
                if (!check) {
//                    majorRegionEto.save()
                }
            }

            cmd.removedItems.each {
                def agreementItem = AgreementSubject.load(new AgreementSubject(agreement: form, subject: Major.load(it)))
                userLogService.log(securityService.userId,securityService.ipAddress,"DELETE", agreementItem,"${agreementItem as JSON}")
                form.removeFromItem(agreementItem)
                agreementItem.delete()
            }
            form.save(flush: true)
            return form
        }
    }

    private findAgreementMajors(Long agreementId) {
        DvAgreementMajorsView.executeQuery'''
select new map(
    amv.id               as id,
    amv.agreementId      as agreementId,
    subject.grade          as grade,
    subject.name         as subjectName,
    department.id        as departmentId,
    department.name      as departmentName,
    amv.majorOptions     as majorOptions
)
from DvAgreementMajorsView amv ,AgreementSubject am 
join am.subject subject 
join subject.subject subject 
join subject.department department
where amv.agreementId = :id and am.id=amv.id
order by department.name, subject.name, subject.grade
''', [id: agreementId]
    }

    private findCoMajors(Long itemId) {
        AgreementCooperativeMajor.executeQuery'''
select new map(
    cm.id           as id,
    cm.nameEn       as nameEn,
    cm.nameCn       as nameCn,
    cm.bachelor     as bachelor    
)
from AgreementCooperativeMajor acm 
join acm.cooperativeMajor cm
where acm.agreementMajor.id = :id
''', [id: itemId]
    }

    /**
     * 特定学院相关协议
     */
    def findAgreementsByDepartment(String departmentId) {
        def list = AgreementSubject.executeQuery'''
select new map(
    subject.id                    as id,
    item.majorOptions           as majorOptions,
    subject.grade                 as grade,
    subject.name                as subjectName,
    gr.name                     as regionName,
    agreement.universityEn      as universityEn,
    agreement.universityCn      as universityCn
)
from AgreementSubject item join item.subject subject 
join subject.subject subject 
join subject.department department
join item.agreement agreement
join agreement.region gr
where department.id = :id
order by subject.name, gr.name, agreement.universityEn, subject.grade, item.majorOptions
''', [id: departmentId]
        List<GroupCondition> conditions = [
                new GroupCondition(
                        groupBy: 'subjectName',
                        into: 'regions',
                        mappings: [
                                subjectName: 'name'
                        ]
                ),
                new GroupCondition(
                        groupBy: 'regionName',
                        into: 'universities',
                        mappings: [
                                regionName: 'name'
                        ]
                ),
                new GroupCondition(
                        groupBy: 'universityEn',
                        into: 'grades',
                        mappings: [
                                universityEn: 'name'
                        ]
                ),
        ]
        return CollectionUtils.groupBy(list, conditions)
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
from CooperativeUniversity c join c.region r
'''
    }
}
