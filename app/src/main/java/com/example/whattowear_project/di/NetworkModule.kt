package com.example.whattowear_project.di

import com.example.whattowear_project.weather.OpenMeteoApi
import com.example.whattowear_project.weather.RetrofitWeatherDataSource
import com.example.whattowear_project.weather.WeatherDataSource
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

/**
 * Hilt модул за мрежата: OkHttp (връзката) → Retrofit (REST) → OpenMeteoApi.
 * Всичко е @Singleton - една връзка за цялото приложение.
 */
@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    private const val TIMEOUT_SECONDS = 15L

    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
        .readTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
        .build()

    @Provides
    @Singleton
    fun provideRetrofit(client: OkHttpClient): Retrofit = Retrofit.Builder()
        .baseUrl(OpenMeteoApi.BASE_URL)
        .client(client)
        .addConverterFactory(GsonConverterFactory.create()) // JSON → Kotlin обекти
        .build()

    @Provides
    @Singleton
    fun provideOpenMeteoApi(retrofit: Retrofit): OpenMeteoApi = retrofit.create(OpenMeteoApi::class.java)
}

/**
 * Когато някой поиска WeatherDataSource (интерфейс), Hilt подава RetrofitWeatherDataSource.
 * В тестовете тук може да се подаде фалшива имплементация.
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class WeatherDataSourceModule {

    @Binds
    @Singleton
    abstract fun bindWeatherDataSource(impl: RetrofitWeatherDataSource): WeatherDataSource
}
