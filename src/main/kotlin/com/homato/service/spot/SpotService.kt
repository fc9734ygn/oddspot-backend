package com.homato.service.spot

import com.github.michaelbull.result.*
import com.homato.data.model.request.SubmitSpotRequest
import com.homato.data.model.response.ExploreSpotResponse
import com.homato.data.model.response.ExploreSpotWithVisitsResponse
import com.homato.data.model.response.SpotsFeedResponse
import com.homato.data.model.response.SubmittedSpotsResponse
import com.homato.data.repository.FileRepository
import com.homato.data.repository.SpotReportRepository
import com.homato.data.repository.SpotRepository
import com.homato.util.getOrElseNotNull
import io.ktor.http.*
import kotlinx.datetime.Clock
import org.koin.core.annotation.Singleton
import org.koin.core.component.KoinComponent
import kotlin.time.Duration.Companion.days

@Singleton
class SpotService(
    private val fileRepository: FileRepository,
    private val spotRepository: SpotRepository,
    private val spotReportRepository: SpotReportRepository
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
            ExploreSpotWithVisitsResponse(
                ExploreSpotResponse.fromSpot(spotWithVisit.spot),
                spotWithVisit.visits)
        }
        val response = SpotsFeedResponse(spotsWithVisitTimestamps)
        return Ok(response)
    }

    suspend fun visitSpot(
        userId: String,
        spotId: Int,
        filePath: String,
        fileContentType: ContentType
    ): Result<Unit, VisitSpotError> {
        val url = fileRepository.uploadImageToBucket(filePath, fileContentType)
            .getOrElse {
                return Err(VisitSpotError.ImageUpload)
            }

        val spot = spotRepository.getSpot(spotId).getOrElseNotNull {
            return Err(VisitSpotError.SpotNotFound)
        }

        if (!spot.isActive) {
            return Err(VisitSpotError.SpotInactive)
        }

        val mostRecentVisit = spotRepository.getAllUserVisits(userId)
            .getOrElse {
                return Err(VisitSpotError.Generic)
            }
            .filter { it.spot_id == spotId }
            .maxByOrNull { it.visit_time }

        val spotVisitCooldown = 1.days
        val lastValidVisitTime = Clock.System.now().minus(spotVisitCooldown).toEpochMilliseconds()

        if (mostRecentVisit != null && mostRecentVisit.visit_time > lastValidVisitTime) {
            return Err(VisitSpotError.SpotRecentlyVisited)
        }

        spotRepository.visitSpot(userId, spotId, url).getOrElse {
            return Err(VisitSpotError.Generic)
        }

        return Ok(Unit)
    }

    suspend fun getSubmittedSpots(userId: String): Result<SubmittedSpotsResponse, Throwable> {
        return spotRepository.getSubmittedSpots(userId).map { SubmittedSpotsResponse(it) }
    }

    suspend fun reportSpot(spotId: Int, reporterId: String, reason: String): Result<Unit, Throwable> {
        return spotReportRepository.addSpotReport(spotId, reporterId, reason)
    }
}