package cn.edu.bnuz.bell.dualdegree

/**
 * 国外专业
 */
class CooperativeMajor {
    /**
     * 英文名缩写
     */
    String shortName
    /**
     * 英文名称
     */
    String nameCn
    /**
     * 中文名称
     */
    String nameEn
    /**
     * 学位（英文）
     */
    String bachelor

    static belongsTo = [university: CooperativeUniversity]

    static mapping = {
        comment                 '国外大学'
        table                   schema: 'tm_dual'
        id                      generator: 'identity', comment: '流水号'
        shortName               length: 5, comment: '英文名缩写'
        nameEn                  length: 100, comment: '英文名称'
        nameCn                  length: 100, comment: '中文名称'
        bachelor                length: 50, comment: '所获得学位'
    }

    static constraints = {
        bachelor    nullable: true
    }
}
