package com.homato.plugins

import com.homato.routes.auth.authenticate
import com.homato.routes.auth.login
import com.homato.routes.auth.register
import com.homato.routes.auth.secretInfo
import com.homato.routes.profile.changeAvatar
import com.homato.routes.profile.changeUsername
import com.homato.routes.profile.deleteAccount
import com.homato.routes.spot.*
import com.homato.routes.wishlist.addToWishlist
import com.homato.routes.wishlist.getWishlist
import com.homato.routes.wishlist.removeFromWishlist
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
        submitSpot()
        spotsFeed()
        submittedSpots()
        visitSpot()
        reportSpot()

        // Profile
        changeUsername()
        deleteAccount()
        changeAvatar()

        // Wishlist
        addToWishlist()
        removeFromWishlist()
        getWishlist()
    }
}
