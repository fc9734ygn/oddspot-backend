package com.homato.data.model.response

import com.homato.data.model.Spot
import com.homato.data.model.Visit
import kotlinx.serialization.Serializable

@Serializable
data class SpotsFeedResponse(
    val spotsWithVisitsResponse: List<ExploreSpotWithVisitsResponse>
)

@Serializable
data class ExploreSpotWithVisitsResponse(
    val spot: ExploreSpotResponse,
    val visits: List<Visit>
)

@Serializable
data class ExploreSpotResponse(
    val id: Int,
    val title: String,
    val description: String,
    val latitude: Double,
    val longitude: Double,
    val creatorId: String,
    val pictureUrl: String,
    val createTime: Long,
    val category: String,
    val difficulty: Int,
    val isArea: Boolean
) {
    companion object {
        fun fromSpot(
            spot: Spot
        ): ExploreSpotResponse {
            return ExploreSpotResponse(
                id = spot.id,
                title = spot.title,
                description = spot.description,
                latitude = spot.latitude,
                longitude = spot.longitude,
                creatorId = spot.creatorId,
                pictureUrl = spot.pictureUrl,
                createTime = spot.createTime,
                category = spot.category,
                difficulty = spot.difficulty,
                isArea = spot.isArea
            )
        }
    }
}
