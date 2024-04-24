package com.homato.routes.wishlist

import com.github.michaelbull.result.fold
import com.github.michaelbull.result.getOrElse
import com.github.michaelbull.result.runCatching
import com.homato.data.model.request.WishlistRequest
import com.homato.routes.COLLECTION_WISHLIST
import com.homato.routes.VERSION_1
import com.homato.routes.getUserId
import com.homato.service.wishlist.WishlistService
import com.homato.util.getOrElseNotNull
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.koin.ktor.ext.inject

fun Route.addToWishlist() {
    val wishListService: WishlistService by inject()

    authenticate {
        post("$VERSION_1/$COLLECTION_WISHLIST/add") {

            val userId = getUserId(call).getOrElse {
                call.respond(HttpStatusCode.Unauthorized, "User not authorized")
                return@post
            }

            val request = call.runCatching { receiveNullable<WishlistRequest>() }.getOrElseNotNull {
                call.respond(HttpStatusCode.BadRequest)
                return@post
            }

            val result = wishListService.addToWishlist(userId, request.spotId)

            result.fold(
                success = {
                    call.respond(HttpStatusCode.OK, it)
                },
                failure = {
                    call.respond(HttpStatusCode.InternalServerError, "Failed to add spot to wishlist")
                }
            )
        }
    }
}