package com.homato.data.repository

import com.github.michaelbull.result.Result
import com.github.michaelbull.result.runCatching
import com.homato.Database
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.koin.core.annotation.Singleton

@Singleton
class WishlistRepository(
    private val database: Database
) {

    suspend fun addToWishlist(userId: String, spotId: Int): Result<Unit, Throwable> = withContext(Dispatchers.IO) {
        runCatching {
            database.wishlistQueries.insertWishlistItem(userId, spotId)
        }
    }

    suspend fun removeFromWishlist(userId: String, spotId: Int): Result<Unit, Throwable> = withContext(Dispatchers.IO) {
        runCatching {
            database.wishlistQueries.deleteWishlistItem(userId, spotId)
        }
    }

    suspend fun getWishlist(userId: String): Result<List<Int>, Throwable> = withContext(Dispatchers.IO) {
        runCatching {
            database.wishlistQueries.selectWishlistByUserId(userId).executeAsList()
        }
    }
}