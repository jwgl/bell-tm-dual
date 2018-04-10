package tm.dualdegree.api

class UrlMappings {

    static mappings = {

        "/agreements"(resources: 'agreement')
        "/agreement-publics"(resources: 'agreementPublic', includes: ['index', 'show'])
        "/settings"(resources: 'setting')

        "/departments"(resources: 'department', includes: []) {
            "/students"(resources: 'studentAbroad')
            "/awards"(resources: 'award') {
                "/applications"(resources: 'applicationAdministrate')
                "/attachments"(controller: 'applicationAdministrate', action: 'attachments', method: 'GET')
            }
            "/mentors"(resources: 'mentor')
            "/agreements"(controller: 'agreementPublic', action: 'agreementsOfDept', method: 'GET')
        }

        "/awards"(resources: 'awardPublic', ['show'])

        "/students"(resources: 'student', includes: []) {
            "/applications"(resources: 'applicationForm') {
                "/approvers"(controller: 'applicationForm', action: 'approvers', method: 'GET')
                "/papers"(resources: 'paperForm')
                "/tousers"(controller: 'paperForm', action: 'tousers', method: 'GET')
                "/workitems"(resources: 'paperForm', includes: ['show', 'patch'])
                collection {
                    "/upload"(controller: 'applicationForm', action: 'upload', method: 'POST')
                }
            }
        }

        "/approvers"(resources: 'approver', includes: []) {
            "/applications"(resources: 'applicationApproval', includes: ['index', 'show', 'update']) {
                "/tousers"(controller: 'applicationApproval', action: 'tousers', method: 'GET')
                "/workitems"(resources: 'applicationApproval', includes: ['show', 'patch'])
                collection {
                    "/mentors"(controller: 'mentor', action: 'index', method: 'GET')
                }
            }
            "/papers"(resources: 'paperApproval') {
                "/workitems"(resources: 'paperApproval', includes: ['show', 'patch'])
                "/upload"(controller: 'paperApproval', action: 'upload', method: 'POST')
                collection {
                    "/attachments"(controller: 'paperApproval', action: 'attachments', method: 'GET')
                }
            }
            "/papermentors"(resources: 'paperMentor') {
                "/tousers"(controller: 'paperMentor', action: 'tousers', method: 'GET')
                "/workitems"(resources: 'paperMentor', includes: ['show', 'patch'])
            }
        }

        "500"(view: '/error')
        "404"(view: '/notFound')
    }
}
