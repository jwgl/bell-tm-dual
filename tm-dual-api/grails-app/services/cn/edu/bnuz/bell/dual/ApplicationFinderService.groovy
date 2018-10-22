package cn.edu.bnuz.bell.dual

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

    def findFinishedByYear(Integer year) {

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
join form.award award
join form.student student
join student.adminClass adminClass
join student.department department
left join form.paperApprover paperApprover
where form.status = 'FINISHED'
and award.requestBegin between to_date(to_char(:year - 1, '9999') || '-08-01', 'YYYY-MM-DD') and to_date(to_char(:year, '9999') || '-07-30', 'YYYY-MM-DD')
order by form.dateSubmitted, student.id
''', [year: year]
    }

    def getFormForReview(Long id) {
        def form = applicationFormService.getFormInfo(id)
        if (!form) {
            throw new ForbiddenException()
        }

        return [
                form: form,
                fileNames: applicationFormService.findFiles(form.studentId as String, form.awardId),
                paperForm: paperFormService.getPaperForm(form.studentId as String, id)
        ]
    }
}
