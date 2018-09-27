package cn.edu.bnuz.bell.dual

class AgreementFilterCommand {
    String name
    String regionName
    Integer grade
    String department
    String subjectId
    String universityCn

    String toString() {
        return "name: ${name}, regionName: ${regionName}, grade: ${grade}, department: ${department}, subjectId: ${subjectId}, universityCn: ${universityCn}"
    }
}
