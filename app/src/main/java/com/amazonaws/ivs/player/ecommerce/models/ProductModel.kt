package com.amazonaws.ivs.player.ecommerce.models

import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

@Serializable
data class ProductModel(
    val id: String,
    val name: String,
    val imageUrl: String,
    val imageLargeUrl: String,
    val price: Int,
    val discountedPrice: Int,
    val longDescription: String,
    @Transient var timeLeft: Int = -1
) {
    val isSelected get() = timeLeft >= 0
    val oldPrice get() = "\$$price"
    val newPrice get() = "\$$discountedPrice"
    val isDiscounted get() = price != discountedPrice
    val timer: String get() = "0:${if (timeLeft > 9) "$timeLeft" else if (timeLeft >= 0) "0$timeLeft" else "00" }"

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as ProductModel
        if (id != other.id) return false
        if (name != other.name) return false
        if (imageUrl != other.imageUrl) return false
        if (imageLargeUrl != other.imageLargeUrl) return false
        if (price != other.price) return false
        if (discountedPrice != other.discountedPrice) return false
        if (longDescription != other.longDescription) return false
        return true
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + name.hashCode()
        result = 31 * result + imageUrl.hashCode()
        result = 31 * result + imageLargeUrl.hashCode()
        result = 31 * result + price
        result = 31 * result + discountedPrice
        result = 31 * result + longDescription.hashCode()
        return result
    }


}
