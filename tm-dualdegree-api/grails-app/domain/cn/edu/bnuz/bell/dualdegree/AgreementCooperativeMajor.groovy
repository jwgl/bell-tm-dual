package cn.edu.bnuz.bell.dualdegree

import org.codehaus.groovy.util.HashCodeHelper

class AgreementCooperativeMajor implements Serializable {
    private static final long serialVersionUID = 1

    /**
     * 国外专业
     */
    CooperativeMajor cooperativeMajor

    static belongsTo = [agreementMajor: AgreementMajor]

    static mapping = {
        comment '协议专业-衔接专业'
        table   schema: 'tm_dual'
        id                  composite: ['agreementMajor', 'cooperativeMajor'], comment: '协议专业-衔接专业ID'
        cooperativeMajor    comment: '衔接专业'
        agreementMajor      comment: '协议专业'
    }

    boolean equals(other) {
        if (!(other instanceof AgreementCooperativeMajor)) {
            return false
        }

        other.cooperativeMajor?.id == cooperativeMajor?.id && other.agreementMajor?.id == agreementMajor?.id
    }

    int hashCode() {
        int hash = HashCodeHelper.initHash()
        hash = HashCodeHelper.updateHash(hash, cooperativeMajor.id)
        hash = HashCodeHelper.updateHash(hash, agreementMajor.id)
        hash
    }

}
