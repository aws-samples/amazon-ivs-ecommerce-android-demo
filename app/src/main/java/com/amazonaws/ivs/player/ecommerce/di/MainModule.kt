package com.amazonaws.ivs.player.ecommerce.di

import android.content.Context
import com.amazonaws.ivs.player.ecommerce.common.JSON_FILE_NAME
import com.amazonaws.ivs.player.ecommerce.common.asObject
import com.amazonaws.ivs.player.ecommerce.common.readJsonAsset
import com.amazonaws.ivs.player.ecommerce.models.ProductsModel
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object MainModule {
    @Provides
    @Singleton
    fun provideProductsModel(@ApplicationContext context: Context) =
        context.readJsonAsset(JSON_FILE_NAME).asObject<ProductsModel>()
}
