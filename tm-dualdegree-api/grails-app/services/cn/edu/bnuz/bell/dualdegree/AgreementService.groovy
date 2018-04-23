package cn.edu.bnuz.bell.dualdegree

import cn.edu.bnuz.bell.dualdegree.eto.MajorRegionEto
import cn.edu.bnuz.bell.master.Major
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
    agreement.universityEn      as      universityEn,
    agreement.universityCn      as      universityCn,
    agreement.memo              as      memo
)
from Agreement agreement join agreement.region gr
join agreement.item item
join item.major major
join major.department department
join major.subject subject
where agreement.name like :name
and gr.name like :regionName
and department.name like :departmentName
and subject.name like :subjectName
and major.grade = :grade
and agreement.universityCn like :universityCn
order by agreement.name
'''
        if (!cmd.grade) queryStr = queryStr.replace('major.grade', ':grade')
        def results = Agreement.executeQuery queryStr,
                [name: cmd.name ? "%${cmd.name}%" : '%',
                 regionName: cmd.regionName ?: '%',
                 departmentName: cmd.department ?: '%',
                 grade: cmd.grade ?: 1,
                 subjectName: cmd.subjectName ?: '%',
                 universityCn: cmd.universityCn ? "%${cmd.name}%" : '%']
        return [list: results, majors: this.majors, regions: this.regions]
    }

    /**
     * 保存协议
     */
    def create(AgreementCommand cmd) {
        Agreement form = new Agreement(
                name:               cmd.agreementName,
                region:             AgreementRegion.load(cmd.regionId),
                universityCn:       cmd.universityCn,
                universityEn:       cmd.universityEn,
                memo:               cmd.memo
        )
        cmd.addedItems.each { item ->
            form.addToItem(new AgreementMajor(
                    major: Major.load(item.id),
                    majorOptions: item.majorOptions,
                    dateCreated: new Date()
            ))

//          添加到自助打印系统中
            def majorRegionEto = new MajorRegionEto(majorId: item.id, region: form.region.name)
            def check = MajorRegionEto.get(majorRegionEto)
            if (!check) {
                majorRegionEto.save()
            }
        }
        form.save()
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
     * 2+2年级专业列表
     */
    def getMajors() {
        Major.executeQuery'''
select new map(
    major.id        as id,
    major.grade     as grade,
    subject.name    as subjectName,
    department.id   as departmentId,
    department.enabled as enabled,
    department.name as departmentName    
)
from Major major join major.subject subject join major.department department
where subject.isDualDegree is true
order by department.name, subject.name, major.grade
'''
    }

    /**
     * 编辑
     */
    def getFormForEdit(Long id) {
        def result = Agreement.executeQuery '''
select new map(
    agreement.id                as      id,
    agreement.name              as      agreementName,
    gr.id                       as      regionId,
    agreement.universityEn      as      universityEn,
    agreement.universityCn      as      universityCn,
    agreement.memo              as      memo
)
from Agreement agreement join agreement.region gr
where agreement.id = :id
''',[id: id]
        if (result) {
            def form = result[0]
            form['items'] = findAgreementMajors(id)
            return form
        } else {
            return []
        }
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
    agreement.universityEn      as      universityEn,
    agreement.universityCn      as      universityCn,
    agreement.memo              as      memo
)
from Agreement agreement join agreement.region gr
where agreement.id = :id
''',[id: id]
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
            form.universityCn = cmd.universityCn
            form.universityEn = cmd.universityEn
            form.region = AgreementRegion.load(cmd.regionId)
            form.memo = cmd.memo

            cmd.addedItems.each { item ->
                def major = Major.load(item.id)
                def agreementItem = AgreementMajor.get(new AgreementMajor(agreement: form, major: major))
                if (!agreementItem) {
                    form.addToItem(new AgreementMajor(
                            major: major,
                            majorOptions: item.majorOptions,
                            dateCreated: new Date()
                    ))
                }

//              添加到自助打印系统中
                def majorRegionEto = new MajorRegionEto(majorId: item.id, region: form.region.name)
                def check = MajorRegionEto.get(majorRegionEto)
                if (!check) {
                    majorRegionEto.save()
                }
            }

            cmd.removedItems.each {
                def agreementItem = AgreementMajor.load(new AgreementMajor(agreement: form, major: Major.load(it)))
                userLogService.log(securityService.userId,securityService.ipAddress,"DELETE", agreementItem,"${agreementItem as JSON}")
                form.removeFromItem(agreementItem)
                agreementItem.delete()
            }
            form.save(flush: true)
            return form
        }
    }

    private findAgreementMajors(Long agreementId) {
        AgreementMajor.executeQuery'''
select new map(
    major.id            as id,
    item.majorOptions   as majorOptions,
    major.grade         as grade,
    subject.name        as subjectName,
    department.id       as departmentId,
    department.name     as departmentName
)
from AgreementMajor item join item.major major join major.subject subject join major.department department
where item.agreement.id = :id
order by department.name, subject.name, major.grade, item.majorOptions
''',[id: agreementId]
    }

    /**
     * 特定学院相关协议
     */
    def findAgreementsByDepartment(String departmentId) {
        def list = AgreementMajor.executeQuery'''
select new map(
    major.id                    as id,
    item.majorOptions           as majorOptions,
    major.grade                 as grade,
    subject.name                as subjectName,
    gr.name                     as regionName,
    agreement.universityEn      as      universityEn,
    agreement.universityCn      as      universityCn
)
from AgreementMajor item join item.major major 
join major.subject subject 
join major.department department
join item.agreement agreement
join agreement.region gr
where department.id = :id
order by subject.name, gr.name, agreement.universityEn, major.grade, item.majorOptions
''',[id: departmentId]
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
}
