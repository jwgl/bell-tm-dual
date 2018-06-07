package tm.dualdegree.web

class UrlMappings {

    static mappings = {

        "/users"(resources: 'user', includes: []) {
            "/agreements"(resources: 'agreementForm', includes: ['index'])
        }

        "/agreements"(resources: 'agreementPublic', includes: ['index'])

        group "/settings", {
            "/users"(resources: 'setting', includes: ['index'])
        }

        group "/admin", {
            "/applications"(resources: 'applicationFinder', includes: ['index'])
        }

        "/departments"(resources: 'department', includes: []) {
            "/students"(resources: 'studentAbroad', includes: ['index'])
            "/awards"(resources: 'award', includes: ['index'])
            "/mentors"(resources: 'mentorForm', includes: ['index'])
            "/agreements"(resources: 'agreementPublicDept', includes: ['index'])
        }

        "/students"(resources: 'student', includes: []) {
            "/applications"(resources: 'applicationForm', includes: ['index'])
        }

        "/approvers"(resources: 'approver', includes: []) {
            "/applications"(resources: 'applicationApproval', includes: ['index'])
            "/papermentors"(resources: 'paperMentor', includes: ['index'])
        }

        "/mentors"(resources: 'approver', includes: []) {
            "/papers"(resources: 'paperApproval', includes: ['index'])
        }

        "/picture"(resource: 'picture', includes: ['show']) {
            collection {
                "/fileview"(action: 'fileView', method: 'GET')
                "/filesrc"(action: 'fileSource', method: 'GET')
                "/download"(action: 'download', method: 'GET')
            }
        }

        "/report"(resource: 'applicationReport', includes: ['show'])


        "/"(view:"/index")
        "500"(view:'/error')
        "404"(view:'/notFound')
    }
}
