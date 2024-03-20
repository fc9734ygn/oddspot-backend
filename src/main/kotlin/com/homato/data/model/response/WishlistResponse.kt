package com.homato.data.model.response

import kotlinx.serialization.Serializable

@Serializable
data class WishlistResponse(
    val spotIds: List<Int>
)