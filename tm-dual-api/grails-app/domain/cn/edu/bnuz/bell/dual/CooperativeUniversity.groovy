package cn.edu.bnuz.bell.dual

/**
 * 国外大学
 */
class CooperativeUniversity {
    /**
     * 英文名缩写
     */
    String shortName
    /**
     * 英文名称
     */
    String nameCn
    /**
     * 中文名称
     */
    String nameEn
    /**
     * 项目分类
     */
    AgreementRegion region

    static hasMany = [cooperativeMajors: CooperativeMajor]

    static mapping = {
        comment                 '国外大学'
        table                   schema: 'tm_dual'
        id                      generator: 'identity', comment: '流水号'
        shortName               length: 10, comment: '英文名缩写'
        nameEn                  length: 100, comment: '英文名称'
        nameCn                  length: 100, comment: '中文名称'
        region                  comment: '项目分类'
    }

    static constraints = {
        nameEn  unique: true
    }
}
