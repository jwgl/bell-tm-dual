package cn.edu.bnuz.bell.dualdegree

class PaperForm {

    /**
     * 申请单
     */
    DegreeApplication form

    /**
     * 类型，本科=1，硕士=2，课程论文=3
     */
    Integer type

    /**
     * 中文题目
     */
    String chineseTitle

    /**
     * 英文题目
     */
    String englishTitle

    /**
     * 互认课程名称
     */
    String name

    static mapping = {
        comment               '论文互认表'
        table                 schema: 'tm_dual'
        id                    generator: 'identity', comment: 'ID'
        type                  comment: '课程类型'
        chineseTitle          length: 100, comment: '中文题目'
        englishTitle          length: 500, comment: '英文题目'
        name                  length: 255, comment: '互认课程名称'
        form                  comment: '关联表单'
    }

    static constraints = {
        name                  nullable: true
    }
}
