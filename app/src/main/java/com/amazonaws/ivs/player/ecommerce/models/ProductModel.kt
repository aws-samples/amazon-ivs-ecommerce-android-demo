package com.amazonaws.ivs.player.ecommerce.models

import com.amazonaws.ivs.player.ecommerce.common.Configuration
import kotlinx.serialization.Serializable

@Serializable
data class ProductModel(
    val id: Int,
    val priceDiscount: String,
    val priceOriginal: String,
    val imageUrl: String,
    val name: String,
    val webLink: String,
    val isFeatured: Boolean,
    val lastPurchaser: PurchaserModel
) {
    fun hasDiscount(): Boolean = priceDiscount.isNotBlank()

    fun getFullImageUrl(): String = Configuration.BASE_URL + imageUrl
}
