package cn.edu.bnuz.bell.dual

import cn.edu.bnuz.bell.organization.Student
import cn.edu.bnuz.bell.organization.Teacher
import cn.edu.bnuz.bell.workflow.State
import cn.edu.bnuz.bell.workflow.StateObject
import cn.edu.bnuz.bell.workflow.StateUserType
import cn.edu.bnuz.bell.workflow.WorkflowInstance

import java.time.LocalDate

/**
 * 学位申请
 */
class DegreeApplication implements StateObject {
    /**
     * 学位授予工作
     */
    Award award

    /**
     * 申请人
     */
    Student student

    /**
     * 联系人
     */
    String linkman

    /**
     * 联系人电话
     */
    String phone

    /**
     * 邮件
     */
    String email

    /**
     * 合作大学
     */
    String universityCooperative

    /**
     * 国外专业
     */
    String majorCooperative

    /**
     * 获得学位年份
     */
    Integer bachelorYear

    /**
     * 创建日期
     */
    LocalDate dateCreated

    /**
     * 状态
     */
    State status

    /**
     * 修改时间
     */
    Date dateModified

    /**
     * 提交时间
     */
    Date dateSubmitted

    /**
     * 论文提交时间
     */
    Date datePaperSubmitted

    /**
     * 论文审批人
     */
    Teacher paperApprover

    /**
     * 论文审批时间
     */
    Date datePaperApproved

    /**
     * 材料审批人
     */
    Teacher approver

    /**
     * 材料审批时间
     */
    Date dateApproved

    /**
     * 工作流实例
     */
    WorkflowInstance workflowInstance

    /**
     * 论文互认表
     */
    static hasOne = [paperForm: PaperForm]

    static mapping = {
        comment                          '出国学生学位申请'
        table                            schema: 'tm_dual'
        id                               generator: 'identity', comment: 'ID'
        award                            comment: '学位授予工作'
        student                          comment: '学生'
        linkman                          length: 20, comment: '导入操作的老师'
        phone                            length: 30, comment: '联系电话'
        email                            length: 50, comment: '邮件'
        universityCooperative            length: 100, comment: '合作大学'
        majorCooperative                 length: 100, comment: '国外专业'
        dateCreated                      comment: '填表的日期'
        status                           sqlType: 'tm.state', type: StateUserType, comment: '状态'
        dateCreated                      comment: '创建时间'
        dateModified                     comment: '修改时间'
        dateSubmitted                    comment: '提交时间'
        datePaperSubmitted               comment: '论文提交时间'
        paperApprover                    comment: '论文审批人'
        datePaperApproved                comment: '论文审批时间'
        approver                         comment: '材料审批人'
        dateApproved                     comment: '材料审批时间'
        workflowInstance                 comment: '工作流实例'
        paperForm                        comment: '论文互认表'
        bachelorYear                     comment: '获得学位年份'
    }
    static constraints = {
        dateSubmitted           nullable: true
        datePaperSubmitted      nullable: true
        paperApprover           nullable: true
        approver                nullable: true
        datePaperApproved       nullable: true
        dateApproved            nullable: true
        workflowInstance        nullable: true
        paperForm               nullable: true
        bachelorYear            nullable: true
    }

    String getWorkflowId() {
        WORKFLOW_ID
    }

    static final WORKFLOW_ID = 'dual.application'
}
