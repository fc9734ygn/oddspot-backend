package com.homato.routes

import com.github.michaelbull.result.runCatching
import com.homato.service.spot.SpotService
import com.homato.util.getOrElseNotNull
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.koin.ktor.ext.inject

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