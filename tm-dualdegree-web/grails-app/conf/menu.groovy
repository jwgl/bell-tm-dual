menuGroup 'main', {
    dualDegreeAdmin 60,{
        agreementAdmin      10, 'PERM_DUALDEGREE_AGREEMENT_WRITE',  '/web/dualdegree/agreements'
        setting             20, 'PERM_DUALDEGREE_ADMIN', '/web/dualdegree/settings'
        agreementPublicDept 30, 'PERM_DUALDEGREE_DEPT_ADMIN', '/web/dualdegree/departments/${departmentId}/agreements'
        studentAbroad       31, 'PERM_DUALDEGREE_DEPT_ADMIN', '/web/dualdegree/departments/${departmentId}/students'
        mentor              32, 'PERM_DUALDEGREE_DEPT_ADMIN', '/web/dualdegree/departments/${departmentId}/mentors'
        award               33, 'PERM_DUALDEGREE_DEPT_ADMIN', '/web/dualdegree/departments/${departmentId}/awards'
        agreementPublic     50, 'PERM_DUALDEGREE_AGREEMENT_READ', '/web/dualdegree/agreement-publics'

    }
    dualDegree 61,{
        application     10, 'PERM_DUALDEGREE_WRITE', '/web/dualdegree/students/${userId}/applications'
    }
    dualDegreeApproval 62,{
        applicationApproval 11, 'PERM_DUALDEGREE_DEPT_ADMIN', '/web/dualdegree/approvers/${userId}/applications'
        paperMentor         12, 'PERM_DUALDEGREE_DEPT_ADMIN', '/web/dualdegree/approvers/${userId}/papermentors'
        paperApproval       13, 'PERM_DUALDEGREE_PAPER_APPROVE', '/web/dualdegree/approvers/${userId}/papers'
    }
}
