menuGroup 'main', {
    dualDegree 60, {
        agreementAdmin      10, 'PERM_DUALDEGREE_AGREEMENT_WRITE',  '/web/dualdegree/users/${userId}/agreements'
        applicationAdmin    11, 'PERM_DUALDEGREE_ADMIN', '/web/dualdegree/admin/applications'
        agreementDept       12, 'PERM_DUALDEGREE_DEPT_ADMIN', '/web/dualdegree/departments/${departmentId}/agreements'
        studentAbroad       13, 'PERM_DUALDEGREE_DEPT_ADMIN', '/web/dualdegree/departments/${departmentId}/students'
        mentor              14, 'PERM_DUALDEGREE_DEPT_ADMIN', '/web/dualdegree/departments/${departmentId}/mentors'
        award               15, 'PERM_DUALDEGREE_DEPT_ADMIN', '/web/dualdegree/departments/${departmentId}/awards'
        apply               16, 'PERM_DUALDEGREE_WRITE', '/web/dualdegree/students/${userId}/applications'
        agreements          17, 'PERM_DUALDEGREE_AGREEMENT_READ', '/web/dualdegree/agreements'
        approval            70, {
            applicationCheck    71, 'PERM_DUALDEGREE_DEPT_ADMIN', '/web/dualdegree/checkers/${userId}/applications'
            paperMentor         72, 'PERM_DUALDEGREE_DEPT_ADMIN', '/web/dualdegree/checkers/${userId}/papers'
            paperApproval       73, 'PERM_DUALDEGREE_PAPER_APPROVE', '/web/dualdegree/mentors/${userId}/papers'
        }
    }
    settings 90, {
        dualDegreeUser      40, 'PERM_DUALDEGREE_ADMIN', '/web/dualdegree/settings/users'
    }
}
