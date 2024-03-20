package com.homato.service.wishlist

import com.github.michaelbull.result.Result
import com.github.michaelbull.result.map
import com.homato.data.model.response.WishlistResponse
import com.homato.data.repository.WishlistRepository
import org.koin.core.annotation.Singleton

@Singleton
class WishlistService(
    private val wishlistRepository: WishlistRepository
) {

    suspend fun addToWishlist(userId: String, spotId: Int): Result<Unit, Throwable> =
        wishlistRepository.addToWishlist(userId, spotId)

    suspend fun removeFromWishlist(userId: String, spotId: Int): Result<Unit, Throwable> =
        wishlistRepository.removeFromWishlist(userId, spotId)

    suspend fun getWishlist(userId: String): Result<WishlistResponse, Throwable> =
        wishlistRepository.getWishlist(userId).map { WishlistResponse(it) }
}