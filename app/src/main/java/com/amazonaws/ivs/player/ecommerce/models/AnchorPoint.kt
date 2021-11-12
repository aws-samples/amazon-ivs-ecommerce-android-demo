package com.amazonaws.ivs.player.ecommerce.models

data class AnchorPoint(
    val x: Float,
    val y: Float,
    val type: AnchorType
)

enum class AnchorType {
    TOP_LEFT,
    TOP_RIGHT,
    BOTTOM_LEFT,
    BOTTOM_RIGHT
}
