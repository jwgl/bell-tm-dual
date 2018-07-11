package tm.dualdegree.api

class UrlMappings {

    static mappings = {

        "/users"(resources: 'user', includes:[]){
            "/agreements"(resources: 'agreement') {
                collection {
                    "/cooperativeMajors"(controller: 'agreement', action: 'findCooperativeMajors', method: 'GET')
                }
            }
            "/universities"(resources: 'cooperativeUniversity')
            "/carryout"(resources: 'agreementCarryout')
        }

        "/agreements"(resources: 'agreementPublic', includes: ['index', 'show'])

        group "/settings", {
            "/users"(resources: 'setting')
        }

        group "/admin", {
            "/applications"(resources: 'applicationFinder')
        }

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
                "/checkers"(controller: 'applicationForm', action: 'checkers', method: 'GET')
                "/papers"(resources: 'paperForm')
                "/tousers"(controller: 'paperForm', action: 'tousers', method: 'GET')
                "/workitems"(resources: 'paperForm', includes: ['show', 'patch'])
                collection {
                    "/upload"(controller: 'applicationForm', action: 'upload', method: 'POST')
                }
            }
        }

        "/checkers"(resources: 'checker', includes: []) {
            "/applications"(resources: 'applicationCheck', includes: ['index', 'show', 'update']) {
                "/tousers"(controller: 'applicationCheck', action: 'tousers', method: 'GET')
                "/workitems"(resources: 'applicationCheck', includes: ['show', 'patch'])
                collection {
                    "/mentors"(controller: 'mentor', action: 'index', method: 'GET')
                }
            }
            "/papermentors"(resources: 'paperMentor') {
                "/tousers"(controller: 'paperMentor', action: 'tousers', method: 'GET')
                "/workitems"(resources: 'paperMentor', includes: ['show', 'patch'])
            }
        }

        "/mentors"(resources: 'mentor', includes: []) {
            "/papers"(resources: 'paperApproval') {
                "/workitems"(resources: 'paperApproval', includes: ['show', 'patch'])
                "/upload"(controller: 'paperApproval', action: 'upload', method: 'POST')
                collection {
                    "/attachments"(controller: 'paperApproval', action: 'attachments', method: 'GET')
                }
            }
        }

        "500"(view: '/error')
        "404"(view: '/notFound')
    }
}
