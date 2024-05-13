package com.homato.data.repository

import com.github.michaelbull.result.runCatching
import com.homato.Database
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.datetime.Clock
import org.koin.core.annotation.Singleton

@Singleton
class VisitRepository(private val database: Database) {

    suspend fun getAllUserVisits(userId: String) = withContext(Dispatchers.IO) {
        runCatching {
            database.visitQueries
                .selectAllWhereUserId(userId)
                .executeAsList()
                .map { visit ->
                    com.homato.data.model.Visit.fromTable(visit)
                }
        }
    }

    suspend fun visitSpot(
        userId: String,
        spotId: Int,
        imageUrl: String?,
        rating: Boolean
    ) = withContext(Dispatchers.IO) {
        runCatching {
            database.visitQueries.insert(
                user_id = userId,
                spot_id = spotId,
                image_url = imageUrl,
                visit_time = Clock.System.now().toEpochMilliseconds(),
                rating = rating
            )
        }
    }
}