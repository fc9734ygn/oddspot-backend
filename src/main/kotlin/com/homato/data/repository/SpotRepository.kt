package com.homato.data.repository

import com.github.michaelbull.result.runCatching
import com.homato.Database
import com.homato.data.model.Spot
import com.homato.data.model.SpotCategory
import com.homato.data.model.SpotVerificationState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.koin.core.annotation.Singleton
import org.koin.core.component.KoinComponent

@Singleton
class SpotRepository(
    private val database: Database
) : KoinComponent {

    suspend fun saveSpot(
        mainImageUrl: String,
        title: String,
        description: String,
        latitude: Double,
        longitude: Double,
        creatorId: String,
        difficulty: Int
    ) = withContext(Dispatchers.IO) {
        runCatching {
            database.spotQueries.insert(
                title = title,
                description = description,
                latitude = latitude,
                longitude = longitude,
                picture_url = mainImageUrl,
                creator_id = creatorId,
                create_time = System.currentTimeMillis(),
                verification_state = SpotVerificationState.SUBMITTED.value,
                category = SpotCategory.ORIGINAL.value,
                difficulty = difficulty,
                is_active = false,
                num_visits = 0,
                last_visited = null,
            )
        }
    }

    suspend fun getAllActiveSpots() = withContext(Dispatchers.IO) {
        runCatching {
            database.spotQueries
                .selectAllWhereIsActiveAndVerificationState(
                    is_active = true,
                    verification_state = SpotVerificationState.VERIFIED.value
                )
                .executeAsList()
                .map { Spot.fromTable(it) }
        }
    }

    suspend fun getAllUserVisits(userId: String) = withContext(Dispatchers.IO) {
        runCatching {
            database.visitQueries
                .selectAllWhereUserId(userId)
                .executeAsList()
        }
    }

    suspend fun visitSpot(
        userId: String,
        spotId: Int,
        imageUrl: String
    ) = withContext(Dispatchers.IO) {
        runCatching {
            database.visitQueries.insert(
                user_id = userId,
                spot_id = spotId,
                image_url = imageUrl,
                visit_time = System.currentTimeMillis()
            )
        }
    }

}