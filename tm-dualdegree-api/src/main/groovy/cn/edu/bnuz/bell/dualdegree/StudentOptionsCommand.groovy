package cn.edu.bnuz.bell.dualdegree

/**
 * 出国学生查询条件选项
 */
class StudentOptionsCommand {
    String  studentId
    String  studentName
    Integer grade
    String  sujectId
    Integer regionId

    def checkValue() {
        return "${studentId}-${studentName}-${grade}-${sujectId}-${regionId}"
    }
}
