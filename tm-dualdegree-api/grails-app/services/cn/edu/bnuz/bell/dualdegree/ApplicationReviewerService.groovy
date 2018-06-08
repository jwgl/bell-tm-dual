package cn.edu.bnuz.bell.dualdegree

import cn.edu.bnuz.bell.http.BadRequestException
import cn.edu.bnuz.bell.workflow.Activities
import cn.edu.bnuz.bell.workflow.ReviewerProvider
import grails.gorm.transactions.Transactional

@Transactional(readOnly = true)
class ApplicationReviewerService implements ReviewerProvider{
    List<Map> getReviewers(Object id, String activity) {
        switch (activity) {
            case Activities.CHECK:
            case 'checkPaper':
                return getCheckers(id as Long)
            case 'approvePaper':
                return getPaperApprovers(id as Long)
            default:
                throw new BadRequestException()
        }
    }

    List<Map> getCheckers(Long id) {
        DegreeApplication.executeQuery'''
select new map(c.id as id, c.name as name)
from DegreeApplication da 
join da.approver c
where da.id = :id
''', [id: id]
    }

    List<Map> getProposer(Long id) {
        DegreeApplication.executeQuery'''
select new map(s.id as id, s.name as name)
from DegreeApplication da 
join da.student s
where da.id = :id
''', [id: id]
    }

    List<Map> getPaperApprovers(Long id) {
        DegreeApplication.executeQuery'''
select new map(c.id as id, c.name as name)
from DegreeApplication da 
join da.paperApprover c
where da.id = :id
''', [id: id]
    }

}
