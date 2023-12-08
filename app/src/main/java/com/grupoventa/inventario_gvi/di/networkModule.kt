package com.grupoventa.inventario_gvi.di

import com.grupoventa.inventario_gvi.core.RetrofitHelper
import com.grupoventa.inventario_gvi.data.network.ItemsApiClient
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object networkModule {
    @Singleton
    @Provides
    fun provideRetrofit():Retrofit{
        val okHttpClient = OkHttpClient.Builder()
            .connectTimeout(30L, TimeUnit.SECONDS)
            .readTimeout(30L, TimeUnit.SECONDS)
            .build()

        return Retrofit.Builder()
            .baseUrl("http://it.gvi.com.mx/api.test/api/")
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    @Singleton
    @Provides
    fun provideItemApiClient(retrofit: Retrofit):ItemsApiClient{
        return retrofit.create(ItemsApiClient::class.java)
    }
}