package com.homato.plugins

import com.homato.routes.*
import io.ktor.server.application.*
import io.ktor.server.routing.*

fun Application.configureRouting() {
    routing {

        // Auth
        login()
        register()
        authenticate()
        secretInfo()

        // Spots
        submitSpotRoute()
        spots()
        submittedSpots()
        visitSpot()

        // Profile
        changeUsername()
    }
}
