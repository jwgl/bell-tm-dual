package cn.edu.bnuz.bell.dualdegree

import cn.edu.bnuz.bell.dualdegree.dv.AgreementCarryoutView
import cn.edu.bnuz.bell.dualdegree.eto.MajorRegionEto
import cn.edu.bnuz.bell.http.BadRequestException
import grails.gorm.transactions.Transactional

@Transactional
class AgreementCarryoutService {

    def list() {
        AgreementCarryoutView.executeQuery'''
select new map(
    subjectId as subjectId,
    subjectName as subjectName,
    regionName as regionName,
    grade as grade,
    majorId as majorId
)
from AgreementCarryoutView 
order by subjectName, grade, regionName
'''
    }

    def create(AgreementCarryoutCommand cmd) {
        def eto = new MajorRegionEto(
                majorId: cmd.majorId,
                region: cmd.regionName
        )
        if (MajorRegionEto.get(eto)) {
            throw new BadRequestException()
        }
        eto.save()
        eto
    }
}
