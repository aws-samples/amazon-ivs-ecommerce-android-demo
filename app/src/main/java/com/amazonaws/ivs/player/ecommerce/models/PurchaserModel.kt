package com.amazonaws.ivs.player.ecommerce.models

import com.amazonaws.ivs.player.ecommerce.common.Configuration
import kotlinx.serialization.Serializable

@Serializable
data class PurchaserModel(
    val username: String,
    val userprofile: String
) {
    fun getFullUserUrl(): String = Configuration.BASE_URL + userprofile
}
