package com.homato.data.model.response

import com.homato.data.model.Spot
import kotlinx.serialization.Serializable

@Serializable
data class SpotWithUserVisitsResponse(
    val spot: Spot,
    val userVisits: List<Long> // List of visit timestamps
)
