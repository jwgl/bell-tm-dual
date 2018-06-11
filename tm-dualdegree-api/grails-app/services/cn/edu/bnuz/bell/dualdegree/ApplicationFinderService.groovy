package cn.edu.bnuz.bell.dualdegree

import cn.edu.bnuz.bell.http.ForbiddenException
import grails.gorm.transactions.Transactional

@Transactional
class ApplicationFinderService {
    ApplicationFormService applicationFormService
    PaperFormService paperFormService

    /**
     * @param q 查询条件
     * @return 查询申请单
     */
    def find(String q) {

        DegreeApplication.executeQuery '''
select new map(
  form.id as id,
  student.id as studentId,
  student.name as studentName,
  student.sex as sex,
  adminClass.name as className,
  form.dateSubmitted as date,
  paperApprover.name as paperApprover,
  form.status as status
)
from DegreeApplication form
join form.student student
join student.adminClass adminClass
join student.department department
left join form.paperApprover paperApprover
where department.name = :q or student.id = :q or student.name = :q
order by form.dateSubmitted, student.id
''', [q: q]
    }

    def getFormForReview(Long id) {
        def form = applicationFormService.getFormInfo(id)
        if (!form) {
            throw new ForbiddenException()
        }

        return [
                form     : form,
                fileNames: applicationFormService.findFiles(form.studentId as String, form.awardId),
                paperForm: paperFormService.getPaperForm(form.studentId as String, id)
        ]
    }
}
