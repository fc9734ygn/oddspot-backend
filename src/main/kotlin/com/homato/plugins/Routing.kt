package com.homato.plugins

import com.homato.routes.authenticate
import com.homato.routes.getSecretInfo
import com.homato.routes.login
import com.homato.routes.register
import com.homato.service.authentication.AuthService
import io.ktor.server.application.*
import io.ktor.server.routing.*

fun Application.configureRouting() {
    routing {
        login()
        register()
        authenticate()
        getSecretInfo()
    }
}
