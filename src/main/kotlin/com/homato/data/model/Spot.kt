package com.homato.data.model

import kotlinx.serialization.Serializable

@Serializable
data class Spot(
    val id: Int,
    val title: String,
    val description: String,
    val latitude: Double,
    val longitude: Double,
    val creatorId: String,
    val pictureUrl: String,
    val createTime: Long,
    val verificationState: String,
    val category: String,
    val difficulty: Int,
    val isActive: Boolean,
    val numVisits: Int,
    val lastVisited: Long?
){
    companion object {
        fun fromTable(spot: com.homato.Spot): Spot {
            return Spot(
                id = spot.id,
                title = spot.title,
                description = spot.description,
                latitude = spot.latitude,
                longitude = spot.longitude,
                creatorId = spot.creator_id,
                pictureUrl = spot.picture_url,
                createTime = spot.create_time,
                verificationState = spot.verification_state,
                category = spot.category,
                difficulty = spot.difficulty,
                isActive = spot.is_active,
                numVisits = spot.num_visits,
                lastVisited = spot.last_visited
            )
        }
    }
}