package com.homato.data.model

import com.homato.SelectAllActiveSpotsWithVisitsAndVerificationState
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
            )
        }
        fun fromQueryResult(row: SelectAllActiveSpotsWithVisitsAndVerificationState): Spot = Spot(
            id = row.id,
            title = row.title,
            description = row.description,
            latitude = row.latitude,
            longitude = row.longitude,
            creatorId = row.creator_id,
            pictureUrl = row.picture_url,
            createTime = row.create_time,
            verificationState = row.verification_state,
            category = row.category,
            difficulty = row.difficulty,
            isActive = row.is_active,
        )
    }
}