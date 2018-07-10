package cn.edu.bnuz.bell.dualdegree.eto

class StudentAbroadEto {
    String studentId
    String studentName
    Date dateCreated
    Date dateDeleted
    String creator
    String deleter
    Boolean enabled
    String region
    static mapping = {
        table           name: 'et_dualdegree_student'
        id              generator: 'sequence',params: [sequence:'student_print_id_seq']
        studentId       length: 20, comment: '学号'
        studentName     length: 50, comment: '姓名'
        dateCreated     comment: '添加日期'
        dateDeleted     comment: '删除日期'
        creator         length: 20, comment: '添加人'
        deleter         length: 20, comment: '删除人'
        enabled         comment: '是否有效'
        region          length: 20, comment: '项目名称'
    }

    static constraints = {
        dateDeleted     nullable: true
        deleter         nullable: true
    }
}
