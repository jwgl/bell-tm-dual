package cn.edu.bnuz.bell.dualdegree

import cn.edu.bnuz.bell.master.Major
import org.codehaus.groovy.util.HashCodeHelper

/**
 *协议专业
 */
class AgreementMajor implements Serializable {
    /**
     * 年级专业
     */
    Major major

    /**
     * 协议
     */
    static belongsTo = [agreement: Agreement]
    static hasMany = [items: AgreementCooperativeMajor]

    Date dateCreated

    static mapping = {
        comment                 '协议适用年级专业'
        table                   schema: 'tm_dual'
        id                      generator: 'identity', comment: '协议专业ID'
        major                   comment: '年级专业'
        agreement               comment: '协议'
    }

    static constraints = {
        dateCreated nullable: true
        major       unique: 'agreement'
    }
}
