package com.homato.data.model.request

import kotlinx.serialization.Serializable

@Serializable
data class VisitSpotRequest(
    val id: Int,
    val rating: Boolean
)
