package cn.edu.bnuz.bell.dual

import cn.edu.bnuz.bell.master.Subject

/**
 *协议专业
 */
class AgreementSubject {
    /**
     * 校内专业
     */
    Subject subject

    /**
     * 起始年级：只做一个门槛，允许比startedGrade低1级的学生选该协议
     */
    Integer startedGrade

    /**
     * 截至年级
     */
    Integer endedGrade

    /**
     * 协议
     */
    Agreement agreement

    static belongsTo = [agreement: Agreement]
    static hasMany = [items: AgreementCooperativeMajor]

    Date dateCreated

    static mapping = {
        comment                 '协议适用年级专业'
        table                   schema: 'tm_dual'
        id                      generator: 'identity', comment: '协议专业ID'
        subject                 comment: '年级专业'
        agreement               comment: '协议'
    }

    static constraints = {
        dateCreated nullable: true
    }
}
