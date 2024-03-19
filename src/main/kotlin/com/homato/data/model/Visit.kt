package com.homato.data.model

import com.homato.SelectAllActiveSpotsWithVisitsAndVerificationState
import kotlinx.serialization.Serializable

@Serializable
data class Visit(
    val id: Int,
    val spotId: Int,
    val userId: String,
    val visitTime: Long,
    val imageUrl: String
) {
    companion object {
        fun fromTable(visit: com.homato.Visit): Visit {
            return Visit(
                id = visit.id,
                spotId = visit.spot_id,
                userId = visit.user_id,
                visitTime = visit.visit_time,
                imageUrl = visit.image_url
            )
        }

        fun fromQueryResult(row: SelectAllActiveSpotsWithVisitsAndVerificationState): Visit? {
            val id = row.id_ ?: return null
            val spotId = row.spot_id ?: return null
            val userId = row.user_id ?: return null
            val visitTime = row.visit_time ?: return null
            val imageUrl = row.image_url ?: return null

            // At this point, id, spotId, userId, visitTime, and imageUrl are all non-nullable
            return Visit(id, spotId, userId, visitTime, imageUrl)
        }
    }
}