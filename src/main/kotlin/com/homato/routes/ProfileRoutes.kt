package com.homato.routes

import com.homato.service.spot.SpotService
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.koin.ktor.ext.inject

fun Route.changeUsername() {
    val spotService: SpotService by inject()

    post("v1/profile/change-username") {
        //TODO: Implement
        val request = call.runCatching { this.receiveNullable<String>() }.getOrNull() ?: kotlin.run {
            call.respond(HttpStatusCode.BadRequest)
            return@post
        }
    }

}