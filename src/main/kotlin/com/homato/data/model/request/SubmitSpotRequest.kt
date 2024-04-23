package com.homato.data.model.request

import kotlinx.serialization.Serializable

@Serializable
data class SubmitSpotRequest(
    val title : String,
    val description : String,
    val latitude : Double,
    val longitude : Double,
    val difficulty : Int,
    val isArea: Boolean
)
