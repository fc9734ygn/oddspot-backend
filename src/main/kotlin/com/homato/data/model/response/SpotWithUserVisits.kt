package com.homato.data.model.response

import com.homato.data.model.Spot

data class SpotWithUserVisits(
    val spot: Spot,
    val userVisits: List<Long>
)
