package cn.edu.bnuz.bell.dualdegree

class AgreementFilterCommand {
    String name
    String regionName
    Integer grade
    String department
    String subjectName
    String universityCn

    String toString() {
        return "name: ${name}, regionName: ${regionName}, grade: ${grade}, department: ${department}, subjectName: ${subjectName}, universityCn: ${universityCn}"
    }
}
