package cn.edu.bnuz.bell.dualdegree.dv

class DvAgreementMajorsView {

    /**
     * 年级专业
     */
    String id

    /**
     * 协议
     */
    Long agreementId

    /**
     * 衔接专业
     */
    String majorOptions

    static mapping = {
        comment      '校内专业-衔接专业视图'
        table        schema: 'tm_dual'
    }

}
