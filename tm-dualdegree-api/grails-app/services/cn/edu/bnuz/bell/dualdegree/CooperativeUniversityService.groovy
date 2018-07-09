package cn.edu.bnuz.bell.dualdegree

import cn.edu.bnuz.bell.utils.CollectionUtils
import cn.edu.bnuz.bell.utils.GroupCondition
import grails.gorm.transactions.Transactional

@Transactional
class CooperativeUniversityService {

    def list() {
        def list = CooperativeUniversity.executeQuery'''
select new map(
    cu.id as cuId,
    cu.shortName as universityShortName,
    cu.nameCn as universityCn,
    cu.nameEn as universityEn,
    rg.name as region,
    cm.shortName as majorShortName,
    cm.nameCn as majorCn,
    cm.nameEn as majorEn,
    cm.bachelor as bachelor
)
from CooperativeUniversity cu 
left join cu.cooperativeMajors cm 
join cu.region rg 
order by rg.name,cu.nameEn
'''
        List<GroupCondition> conditions = [
                new GroupCondition(
                        groupBy: 'cuId',
                        into: 'majors',
                        mappings: [
                                cuId:                   'cuId',
                                universityShortName:    'shortName',
                                universityEn:           'universityEn',
                                universityCn:           'universityCn',
                                region:                 'region'
                        ]
                ),
        ]
        return CollectionUtils.groupBy(list, conditions)
    }

    /**
     * 保存合作大学
     */
    def create(CooperativeUniversityCommand cmd) {
        CooperativeUniversity form = new CooperativeUniversity(
                nameEn:             cmd.nameEn,
                shortName:          cmd.shortName.length() > 10 ? getShortName(cmd.nameEn) : cmd.shortName,
                region:             AgreementRegion.load(cmd.regionId),
                nameCn:             cmd.nameCn
        )
        cmd.addedItems.each { item ->
            form.addToCooperativeMajors(new CooperativeMajor(
                    shortName: getShortName(item.nameEn),
                    nameEn: item.nameEn,
                    nameCn: item.nameCn,
                    bachelor: item.bachelor
            ))
        }
        form.save()
        return form
    }

    /**
     * 更新合作大学
     */
    def update(CooperativeUniversityCommand cmd) {
        CooperativeUniversity form = CooperativeUniversity.load(cmd.id)
        if (form) {
            form.nameEn = cmd.nameEn
            form.shortName = cmd.shortName
            form.region = AgreementRegion.load(cmd.regionId)
            form.nameCn = cmd.nameCn

            cmd.addedItems.each { item ->
                def cooperativeMajor = CooperativeMajor.findByUniversityAndNameEn(form, item.nameEn)
                if (!cooperativeMajor) {
                    form.addToCooperativeMajors(new CooperativeMajor(
                            shortName: getShortName(item.nameEn),
                            nameEn: item.nameEn,
                            nameCn: item.nameCn,
                            bachelor: item.bachelor
                    ))
                }
            }
            form.save(flush: true)
            return form
        }
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
     * 编辑
     */
    def getFormForEdit(Long id) {
        def result = CooperativeUniversity.executeQuery'''
select new map(
    cu.id as id,
    cu.shortName as shortName,
    cu.nameCn as nameCn,
    cu.nameEn as nameEn,
    rg.id as regionId
)
from CooperativeUniversity cu 
join cu.region rg 
where cu.id = :id
''', [id: id]
        if (result) {
            def form = result[0]
            form['items'] = findCooperativeMajors(id)
            return form
        } else {
            return []
        }
    }

    def findCooperativeMajors(Long id) {
        CooperativeMajor.executeQuery'''
select new map(
    item.id               as id,
    item.nameEn           as nameEn,
    item.shortName        as shortName,
    item.nameCn           as nameCn,
    item.bachelor         as bachelor
)
from CooperativeMajor item
where item.university.id = :id
order by item.bachelor, item.nameEn
''', [id: id]
    }

    /**
     * 浏览
     */
    def getFormForShow(Long id) {
        def result = CooperativeUniversity.executeQuery '''
select new map(
    cu.id as id,
    cu.shortName as shortName,
    cu.nameCn as nameCn,
    cu.nameEn as nameEn,
    rg.name as region
)
from CooperativeUniversity cu 
join cu.region rg 
where cu.id = :id
''', [id: id]
        if (result) {
            def form = result[0]
            form['items'] = findCooperativeMajors(id)
            return form
        } else {
            return []
        }
    }

    /**
     * 英文名缩写的统一算法
     */
    private getShortName(String nameEn) {
        def words = nameEn.trim().split(" ")
        def shortName = ""
        if (words.length > 1) {
            words.each {
                if (it.length()) {
                    shortName += it.charAt(0).toUpperCase()
                }
            }
        }  else if (nameEn.trim().length() > 3){
            shortName = nameEn.trim().substring(0,3).toUpperCase()
        } else {
            shortName = nameEn.trim()
        }
        return shortName
    }
}
