package cn.edu.bnuz.bell.dual

/**
 * 协议
 */
class Agreement {
    /**
     * 协议名称
     */
    String name

    /**
     * 备注
     */
    String memo

    /**
     * 协议专业
     */
    static hasMany = [item: AgreementSubject]

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
        university              comment: '合作大学'
    }

    static constraints = {
        memo         nullable: true
    }
}
