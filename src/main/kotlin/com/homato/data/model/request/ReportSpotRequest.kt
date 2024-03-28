package com.homato.data.model.request

import kotlinx.serialization.Serializable

@Serializable
data class ReportSpotRequest(
    val spotId: Int,
    val reason: String
)
