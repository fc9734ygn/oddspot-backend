package com.homato.service.spot

import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import com.github.michaelbull.result.Result
import com.github.michaelbull.result.getOrElse
import com.homato.data.model.request.SubmitSpotRequest
import com.homato.data.model.response.AllSpotsResponse
import com.homato.data.model.response.SpotWithUserVisits
import com.homato.data.repository.FileRepository
import com.homato.data.repository.SpotRepository
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

    suspend fun getSpots(userId: String): Result<AllSpotsResponse, Throwable> {
        val spots = spotRepository.getAllActiveSpots().getOrElse {
            return Err(it)
        }
        val allUserVisits = spotRepository.getAllUserVisits(userId).getOrElse {
            return Err(it)
        }
        // If performance becomes an issue optimise this or do this in SQL query
        val spotsWithUserVisits = spots.map { spot ->
            val userVisitsOfTheSpot = allUserVisits.filter { it.spot_id == spot.id }.map { it.visit_time }
            SpotWithUserVisits(spot, userVisitsOfTheSpot)
        }
        return Ok(AllSpotsResponse(spotsWithUserVisits))
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
        return spotRepository.visitSpot(userId, spotId, url)
    }
}