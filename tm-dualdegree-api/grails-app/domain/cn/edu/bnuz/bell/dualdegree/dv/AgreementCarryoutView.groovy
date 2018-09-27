package cn.edu.bnuz.bell.dualdegree.dv

/**
 * 可执行协议专业视图
 */
class AgreementCarryoutView {
    /**
     * 专业Id
     */
    String subjectId

    /**
     * 专业名称
     */
    String subjectName

    /**
     *项目名称
     */
    String regionName

    /**
     * 可执行年级
     */
    Integer grade

    /**
     * 已执行年级专业ID
     */
    String majorId

    static mapping = {
        comment      '可执行协议专业视图'
        table        name: 'dv_agreement_carryout_view', schema: 'tm_dual'
    }
}
