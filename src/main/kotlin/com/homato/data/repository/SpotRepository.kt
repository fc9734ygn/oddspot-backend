package com.homato.data.repository

import com.github.michaelbull.result.Result
import com.github.michaelbull.result.runCatching
import com.homato.Database
import com.homato.data.model.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.datetime.Clock
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
                create_time = Clock.System.now().toEpochMilliseconds(),
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

    suspend fun getAllActiveAndVerifiedSpotsWithVisitsForUser(userId: String): Result<List<SpotWithVisits>, Throwable> =
        withContext(Dispatchers.IO) {
            runCatching {

                val flatQueryResults = database.spotQueries
                    .selectAllActiveSpotsWithVisitsByUserIdAndVerificationState(
                        is_active = true,
                        verification_state = SpotVerificationState.VERIFIED.value,
                        user_id = userId
                    )
                    .executeAsList()

                flatQueryResults.groupBy { it.id } // Group by spot's ID
                    .map { (_, spotVisitsRows) ->
                        val firstRow = spotVisitsRows.first()
                        val spot = Spot.fromQueryResult(firstRow)
                        val visits = spotVisitsRows.mapNotNull { Visit.fromQueryResult(it) }
                        SpotWithVisits(spot, visits)
                    }
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
                visit_time = Clock.System.now().toEpochMilliseconds()
            )
        }
    }

    suspend fun getSpot(spotId: Int) = withContext(Dispatchers.IO) {
        runCatching {
            val entity = database.spotQueries.selectById(spotId).executeAsOne()
            Spot.fromTable(entity)
        }
    }

    suspend fun getSubmittedSpots(userId: String) = withContext(Dispatchers.IO) {
        runCatching {
            database.spotQueries
                .selectAllWhereCreatorId(userId)
                .executeAsList()
                .map { Spot.fromTable(it) }
        }
    }

}