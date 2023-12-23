package com.homato.routes

import com.github.michaelbull.result.runCatching
import com.homato.service.spot.SpotService
import com.homato.util.getOrElseNotNull
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.util.cio.*
import io.ktor.utils.io.*
import org.koin.ktor.ext.inject
import java.io.File

fun Route.submitSpotRoute() {
    val spotService: SpotService by inject()

    authenticate {
        post("v1/spot/submit-spot") {

            val userId = getUserId(call)
            if (userId == null) {
                call.respond(HttpStatusCode.Unauthorized, "User not authorized")
                return@post
            }
            spotService.submitSpot()

//            val file = File("uploads/ktor_logo.png")
//            call.receiveChannel().copyAndClose(file.writeChannel())
//            call.respondText("A file is uploaded")



            call.respond(HttpStatusCode.OK, "Spot and image submitted successfully")
        }
    }
}

fun Route.unknownSpots() {
    val spotService: SpotService by inject()

    get("v1/spot/unknown-spots") {
        //TODO: Implement

        call.respondText("Hello World!")
    }
}

fun Route.visitedSpots() {
    val spotService: SpotService by inject()

    get("v1/spot/visited-spots") {
        //TODO: Implement
        val request = call.runCatching { this.receiveNullable<String>() }.getOrElseNotNull {
            call.respond(HttpStatusCode.BadRequest)
            return@get
        }

        call.respondText("Hello World!")
    }
}

fun Route.submittedSpots() {
    //TODO: Implement
    val spotService: SpotService by inject()

    get("v1/spot/submitted-spots") {
        val request = call.runCatching { this.receiveNullable<String>() }.getOrElseNotNull {
            call.respond(HttpStatusCode.BadRequest)
            return@get
        }

        call.respondText("Hello World!")
    }
}