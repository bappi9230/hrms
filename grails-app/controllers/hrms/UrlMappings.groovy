package hrms

class UrlMappings {

    static mappings = {
        // master data screens: /master/branch, /master/shift/edit/3 ...
        "/master/$type/$action?/$id?"(controller: 'master')

        "/$controller/$action?/$id?(.$format)?"{
            constraints {
                // apply constraints here
            }
        }

        "/"(controller: 'dashboard', action: 'index')
        "500"(view:'/error')
        "404"(view:'/notFound')
    }
}