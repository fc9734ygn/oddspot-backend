package com.homato.routes.auth

import com.github.michaelbull.result.getOrElse
import com.homato.routes.COLLECTION_AUTH
import com.homato.routes.VERSION_1
import com.homato.routes.getUserId
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.secretInfo() {
    authenticate {
        get("$VERSION_1/$COLLECTION_AUTH/secret-info") {
            val userId = getUserId(call).getOrElse {
                call.respond(HttpStatusCode.Unauthorized, "User not authorized")
                return@get
            }
            call.respond(HttpStatusCode.OK, "Your userId is $userId")
        }
    }
}