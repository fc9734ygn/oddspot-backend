package com.homato.data.model.response

import com.homato.data.model.Spot
import kotlinx.serialization.Serializable

@Serializable
data class SubmittedSpotsResponse(
    val spots: List<Spot>
)