package cn.edu.bnuz.bell.dualdegree

/**
 * 合作大学所在区域
 */
class AgreementRegion {
    /**
     *区域名称
     */
    String        name

    static mapping = {
        comment                 '合作大学所在区域，俗称项目'
        table                   schema: 'tm_dual'
        id                      generator: 'identity', comment: '区域ID'
        name                    length: 50, comment: '区域名称'
    }

}
