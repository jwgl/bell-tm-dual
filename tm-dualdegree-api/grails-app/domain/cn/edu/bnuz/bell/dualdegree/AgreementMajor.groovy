package cn.edu.bnuz.bell.dualdegree

import cn.edu.bnuz.bell.master.Major

/**
 *协议专业
 */
class AgreementMajor implements Serializable {
    /**
     * 年级专业
     */
    Major major

    /**
     * 可衔接国外专业
     */
    String majorOptions

    /**
     * 协议
     */
    static belongsTo = [agreement: Agreement]

    Date dateCreated

    static mapping = {
        comment                 '协议适用年级专业'
        table                   schema: 'tm_dual'
        id                      composite: ['agreement', 'major']
    }

    static constraints = {
        dateCreated nullable: true
    }
}
