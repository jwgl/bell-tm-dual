package cn.edu.bnuz.bell.dualdegree.eto

class MajorRegionEto implements Serializable{
    String majorId
    String region

    static mapping = {
        table   name: 'et_dualdegree_major_region'
        majorId length: 20, comment: '教学计划号'
        region  length: 50, comment: '项目名称'
        id      composite: ['majorId', 'region']
    }
}
