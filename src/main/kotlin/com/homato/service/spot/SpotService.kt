package com.homato.service.spot

import com.github.michaelbull.result.*
import com.homato.BACKBLAZE_SPOT_MAIN_IMAGE_BUCKET_ID
import com.homato.BACKBLAZE_SPOT_MAIN_IMAGE_BUCKET_NAME
import com.homato.BACKBLAZE_SPOT_VISIT_IMAGE_BUCKET_ID
import com.homato.BACKBLAZE_SPOT_VISIT_IMAGE_BUCKET_NAME
import com.homato.data.model.request.SubmitSpotRequest
import com.homato.data.model.response.ExploreSpotResponse
import com.homato.data.model.response.ExploreSpotWithVisitsResponse
import com.homato.data.model.response.SpotsFeedResponse
import com.homato.data.model.response.SubmittedSpotsResponse
import com.homato.data.repository.FileRepository
import com.homato.data.repository.SpotReportRepository
import com.homato.data.repository.SpotRepository
import com.homato.data.repository.VisitRepository
import com.homato.util.Environment
import com.homato.util.getOrElseNotNull
import io.ktor.http.*
import org.koin.core.annotation.Singleton
import org.koin.core.component.KoinComponent

@Singleton
class SpotService(
    private val fileRepository: FileRepository,
    private val spotRepository: SpotRepository,
    private val spotReportRepository: SpotReportRepository,
    private val visitRepository: VisitRepository,
    private val environment: Environment
) : KoinComponent {

    suspend fun submitSpot(
        filePath: String,
        spotData: SubmitSpotRequest,
        creatorId: String,
        contentType: ContentType
    ): Result<Unit, Throwable> {
        val url = fileRepository.uploadImageToBucket(
            filePath,
            contentType,
            environment.getVariable(BACKBLAZE_SPOT_MAIN_IMAGE_BUCKET_ID),
            environment.getVariable(BACKBLAZE_SPOT_MAIN_IMAGE_BUCKET_NAME)
        ).getOrElse {
            return Err(it)
        }

        return spotRepository.saveSpot(
            mainImageUrl = url,
            title = spotData.title,
            description = spotData.description,
            latitude = spotData.latitude,
            longitude = spotData.longitude,
            creatorId = creatorId,
            difficulty = spotData.difficulty,
            isArea = spotData.isArea
        )
    }

    suspend fun getSpotsFeed(): Result<SpotsFeedResponse, Throwable> {
        val spotsWithVisits = spotRepository.getAllActiveAndVerifiedSpotsWithVisits().getOrElse {
            return Err(it)
        }
        val data = spotsWithVisits.map { spotWithVisit ->
            ExploreSpotWithVisitsResponse(
                ExploreSpotResponse.fromSpot(spotWithVisit.spot),
                spotWithVisit.visits
            )
        }
        val response = SpotsFeedResponse(data)
        return Ok(response)
    }

    suspend fun visitSpot(
        userId: String,
        spotId: Int,
        filePath: String?,
        fileContentType: ContentType?,
        rating: Boolean,
    ): Result<Unit, VisitSpotError> {

        val spot = spotRepository.getSpot(spotId).getOrElseNotNull {
            return Err(VisitSpotError.SpotNotFound)
        }

        if (!spot.isActive) {
            return Err(VisitSpotError.SpotInactive)
        }

        val spotAlreadyVisitedByUser = visitRepository.getAllUserVisits(userId)
            .getOrElse {
                return Err(VisitSpotError.Generic)
            }.any {
                it.spotId == spotId
            }

        if (spotAlreadyVisitedByUser) {
            return Err(VisitSpotError.SpotAlreadyVisited)
        }

        val url = if (filePath != null && fileContentType != null) {
            fileRepository.uploadImageToBucket(
                filePath,
                fileContentType,
                environment.getVariable(BACKBLAZE_SPOT_VISIT_IMAGE_BUCKET_ID),
                environment.getVariable(BACKBLAZE_SPOT_VISIT_IMAGE_BUCKET_NAME)
            ).getOrElse {
                return Err(VisitSpotError.ImageUpload)
            }
        } else {
            null
        }

        visitRepository.visitSpot(userId, spotId, url, rating).getOrElse {
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