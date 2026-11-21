package com.example.whattowear_project.weather

import com.example.whattowear_project.weather.dto.ForecastResponse
import com.example.whattowear_project.weather.dto.GeocodingResponse
import retrofit2.http.GET
import retrofit2.http.Query

/**
 * Retrofit интерфейс към безплатното REST API на Open-Meteo (не иска API ключ).
 * Ние само описваме адреса и параметрите - Retrofit сам пише кода за HTTP заявката.
 */
interface OpenMeteoApi {

    /** Прогноза за координати: текущото време + мин/макс и шанс за дъжд за днес. */
    @GET("v1/forecast")
    suspend fun getForecast(
        @Query("latitude") latitude: Double,
        @Query("longitude") longitude: Double,
        @Query("current") current: String =
            "temperature_2m,apparent_temperature,precipitation,weather_code,is_day,wind_speed_10m",
        @Query("daily") daily: String =
            "temperature_2m_min,temperature_2m_max,precipitation_probability_max",
        @Query("timezone") timezone: String = "auto",
        @Query("forecast_days") forecastDays: Int = 1,
    ): ForecastResponse

    /**
     * Търсене на град по име. Друг хост - затова е пълен адрес
     * (Retrofit позволява пълен URL в @GET вместо base URL).
     */
    @GET("https://geocoding-api.open-meteo.com/v1/search")
    suspend fun searchCity(
        @Query("name") name: String,
        @Query("count") count: Int = 10,
        @Query("language") language: String = "bg",
        @Query("format") format: String = "json",
    ): GeocodingResponse

    companion object {
        const val BASE_URL = "https://api.open-meteo.com/"
    }
}
