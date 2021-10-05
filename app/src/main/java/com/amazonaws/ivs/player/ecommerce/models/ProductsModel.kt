package com.amazonaws.ivs.player.ecommerce.models

import kotlinx.serialization.Serializable

@Serializable
data class ProductsModel(val products: List<ProductModel>)
