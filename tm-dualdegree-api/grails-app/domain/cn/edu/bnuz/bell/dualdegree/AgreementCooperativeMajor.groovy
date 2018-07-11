package cn.edu.bnuz.bell.dualdegree

class AgreementCooperativeMajor implements Serializable {
    /**
     * 协议专业
     */
    AgreementSubject agreementMajor

    /**
     * 国外专业
     */
    CooperativeMajor cooperativeMajor

    static belongsTo = [agreementMajor: AgreementSubject]

    static mapping = {
        comment             '协议专业-衔接专业'
        table               schema: 'tm_dual'
        id                  generator: 'identity', comment: '协议专业-衔接专业ID'
        cooperativeMajor    comment: '衔接专业'
        agreementMajor      comment: '协议专业'
    }
}
