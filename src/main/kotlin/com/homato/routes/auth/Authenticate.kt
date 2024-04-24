package com.homato.routes.auth

import com.homato.routes.COLLECTION_AUTH
import com.homato.routes.VERSION_1
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.authenticate() {
    authenticate {
        get("$VERSION_1/$COLLECTION_AUTH/authenticate") {
            call.respond(HttpStatusCode.OK)
        }
    }
}