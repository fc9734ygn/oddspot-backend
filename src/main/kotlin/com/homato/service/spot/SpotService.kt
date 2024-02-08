package com.homato.service.spot

import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import com.github.michaelbull.result.Result
import com.github.michaelbull.result.getOrElse
import com.homato.data.model.request.SubmitSpotRequest
import com.homato.data.model.response.SpotWithUserVisitsResponse
import com.homato.data.model.response.SpotsFeedResponse
import com.homato.data.repository.FileRepository
import com.homato.data.repository.SpotRepository
import com.homato.util.getOrElseNotNull
import io.ktor.http.*
import org.koin.core.annotation.Singleton
import org.koin.core.component.KoinComponent

@Singleton
class SpotService(
    private val fileRepository: FileRepository,
    private val spotRepository: SpotRepository
) : KoinComponent {

    suspend fun submitSpot(
        filePath: String,
        spotData: SubmitSpotRequest,
        creatorId: String,
        contentType: ContentType
    ): Result<Unit, Throwable> {
        val url = fileRepository.uploadImageToBucket(filePath, contentType).getOrElse {
            return Err(it)
        }
        return spotRepository.saveSpot(
            mainImageUrl = url,
            title = spotData.title,
            description = spotData.description,
            latitude = spotData.latitude,
            longitude = spotData.longitude,
            creatorId = creatorId,
            difficulty = spotData.difficulty
        )
    }

    suspend fun getSpotsFeed(userId: String): Result<SpotsFeedResponse, Throwable> {
        val spotsWithVisits = spotRepository.getAllActiveAndVerifiedSpotsWithVisitsForUser(userId).getOrElse {
            return Err(it)
        }
        val spotsWithVisitTimestamps = spotsWithVisits.map { spotWithVisit ->
            SpotWithUserVisitsResponse(spotWithVisit.spot, spotWithVisit.visits.map { it.visitTime })
        }
        val response = SpotsFeedResponse(spotsWithVisitTimestamps)
        return Ok(response)
    }

    suspend fun visitSpot(
        userId: String,
        spotId: Int,
        filePath: String,
        fileContentType: ContentType
    ): Result<Unit, Throwable> {
        val url = fileRepository.uploadImageToBucket(filePath, fileContentType)
            .getOrElse {
                return Err(it)
            }
        val spot = spotRepository.getSpot(spotId).getOrElseNotNull {
            return Err(it ?: Throwable("Spot not found"))
        }
        if (!spot.is_active) {
            return Err(Throwable("Spot is not active"))
        }
        return spotRepository.visitSpot(userId, spotId, url)
    }
}