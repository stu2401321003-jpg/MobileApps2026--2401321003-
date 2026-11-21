package com.example.whattowear_project.weather.dto

import com.google.gson.annotations.SerializedName

/*
 * DTO (Data Transfer Object) класове - повтарят ТОЧНО формата на JSON-а от Open-Meteo.
 * Gson ги попълва автоматично; @SerializedName казва кое JSON поле в кое Kotlin поле отива.
 * Полетата са nullable, защото не вярваме сляпо на сървъра - проверяваме ги в mapper-а.
 */

// ---------- Търсене на град (geocoding-api.open-meteo.com/v1/search) ----------

data class GeocodingResponse(
    @SerializedName("results") val results: List<GeocodingResultDto>?,
)

data class GeocodingResultDto(
    @SerializedName("id") val id: Long?,
    @SerializedName("name") val name: String?,
    @SerializedName("latitude") val latitude: Double?,
    @SerializedName("longitude") val longitude: Double?,
    @SerializedName("country") val country: String?,
    /** Област, напр. „Пловдив“ или „Област Смолян“. */
    @SerializedName("admin1") val region: String?,
)

// ---------- Прогноза (api.open-meteo.com/v1/forecast) ----------

data class ForecastResponse(
    @SerializedName("current") val current: CurrentWeatherDto?,
    @SerializedName("daily") val daily: DailyWeatherDto?,
)

data class CurrentWeatherDto(
    @SerializedName("temperature_2m") val temperature: Double?,
    @SerializedName("apparent_temperature") val apparentTemperature: Double?,
    /** Валежи в момента (mm). */
    @SerializedName("precipitation") val precipitation: Double?,
    /** WMO код на времето (0 = ясно, 61 = дъжд, 71 = сняг...). */
    @SerializedName("weather_code") val weatherCode: Int?,
    /** 1 = ден, 0 = нощ. */
    @SerializedName("is_day") val isDay: Int?,
    @SerializedName("wind_speed_10m") val windSpeed: Double?,
)

/** Дневните стойности идват като масиви - по един елемент за всеки ден (ние искаме само днес). */
data class DailyWeatherDto(
    @SerializedName("temperature_2m_min") val minTemperature: List<Double>?,
    @SerializedName("temperature_2m_max") val maxTemperature: List<Double>?,
    @SerializedName("precipitation_probability_max") val precipitationProbability: List<Int?>?,
)
