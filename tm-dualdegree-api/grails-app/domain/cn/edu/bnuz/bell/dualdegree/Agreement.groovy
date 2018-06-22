package cn.edu.bnuz.bell.dualdegree

/**
 * 协议
 */
class Agreement {
    /**
     * 协议名称
     */
    String name

    /**
     * 项目分类 由前台定义好map
     */
    AgreementRegion region

    /**
     * 国外大学名称（英文）
     */
    String universityEn

    /**
     * 国外大学名称（中文）
     */
    String universityCn

    /**
     * 备注
     */
    String memo

    /**
     * 协议专业
     */
    static hasMany = [item: AgreementMajor]

    /**
     * 合作大学
     */
    CooperativeUniversity university

    static mapping = {
        comment                 '协议'
        table                   schema: 'tm_dual'
        id                      generator: 'identity', comment: '协议ID'
        name                    length: 500, comment: '协议名称'
        memo                    length: 1000,comment: '备注'
        region                  comment: '协议按地区分类'
        universityEn            length: 200, comment: '国外大学英文名'
        universityCn            length: 100, comment: '国外大学英文名'
        university              comment: '合作大学'
    }

    static constraints = {
        memo         nullable: true
        university   nullable: true
    }
}
