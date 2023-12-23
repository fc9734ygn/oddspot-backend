package com.homato.data.repository

import com.github.michaelbull.result.runCatching
import com.homato.Database
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
        creatorId: String
    ) = withContext(Dispatchers.IO) {
        runCatching {
            database.spotQueries.insert(title = title,
                description = description,
                latitude = latitude,
                longitude = longitude,
                picture_url = mainImageUrl,
                creator_id = creatorId,
                create_time = System.currentTimeMillis(),
                verification_state = SpotVerificationState.SUBMITTED.value,
                category = SpotCategory.ORIGINAL.value,
                is_active = false,
                num_visits = 0,
                last_visited = null
            )
        }
    }
}