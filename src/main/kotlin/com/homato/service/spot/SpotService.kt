package com.homato.service.spot

import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Result
import com.github.michaelbull.result.getOrElse
import com.homato.data.model.request.SubmitSpotRequest
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
    ) : Result<Unit, Throwable> {
        val url = fileRepository.uploadSpotMainImage(filePath, contentType).getOrElse {
            return Err(it)
        }
         return spotRepository.saveSpot(
            mainImageUrl = url,
            title = spotData.title,
            description = spotData.description,
            latitude = spotData.latitude,
            longitude = spotData.longitude,
            creatorId = creatorId
        )
    }
}