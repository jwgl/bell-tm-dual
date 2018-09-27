package cn.edu.bnuz.bell.dual

import cn.edu.bnuz.bell.dual.dv.AgreementCarryoutView
import cn.edu.bnuz.bell.dual.eto.MajorRegionEto
import cn.edu.bnuz.bell.http.BadRequestException
import grails.gorm.transactions.Transactional

@Transactional
class AgreementCarryoutService {

    def list() {
        AgreementCarryoutView.executeQuery'''
select new map(
    subjectId as subjectId,
    subjectName as subjectId,
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
