package com.amazonaws.ivs.player.ecommerce.models

import kotlinx.serialization.Serializable

@Serializable
data class MetadataModel(val products: List<ProductModel>)
