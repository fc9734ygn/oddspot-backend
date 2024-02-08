package com.homato.data.model.response

import kotlinx.serialization.Serializable

@Serializable
data class SpotsFeedResponse(
    val spots: List<SpotWithUserVisitsResponse>
)
