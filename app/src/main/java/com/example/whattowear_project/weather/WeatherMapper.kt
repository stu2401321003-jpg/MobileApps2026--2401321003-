package com.example.whattowear_project.weather

import com.example.whattowear_project.weather.dto.ForecastResponse
import com.example.whattowear_project.weather.dto.GeocodingResultDto
import com.example.whattowear_project.weather.model.City
import com.example.whattowear_project.weather.model.Weather
import com.example.whattowear_project.weather.model.WeatherCondition
import kotlin.math.roundToInt

/**
 * Превръща суровите DTO от Open-Meteo в моделите на приложението.
 * Чиста логика (без мрежа и Android) - лесно се тества с Unit тестове.
 */
object WeatherMapper {

    /** От този шанс за валежи нататък смятаме, че днес ще вали. */
    const val RAIN_PROBABILITY_THRESHOLD = 50

    fun toWeather(response: ForecastResponse, city: City): Weather {
        val current = requireNotNull(response.current) { "Липсва текущото време" }
        val temperature = requireNotNull(current.temperature) { "Липсва температура" }.roundToInt()
        val daily = response.daily

        val condition = WeatherCondition.fromWmoCode(current.weatherCode)
        val precipitationProbability = daily?.precipitationProbability?.firstOrNull() ?: 0
        val isRainingNow = (current.precipitation ?: 0.0) > 0.0

        return Weather(
            city = city,
            temperature = temperature,
            feelsLike = current.apparentTemperature?.roundToInt() ?: temperature,
            minTemperature = daily?.minTemperature?.firstOrNull()?.roundToInt() ?: temperature,
            maxTemperature = daily?.maxTemperature?.firstOrNull()?.roundToInt() ?: temperature,
            precipitationProbability = precipitationProbability,
            windSpeed = current.windSpeed?.roundToInt() ?: 0,
            condition = condition,
            isDay = current.isDay != 0,
            // Вали в момента, ИЛИ кодът е дъжд, ИЛИ голям шанс за валежи днес.
            isRainy = isRainingNow ||
                condition.isRainy ||
                precipitationProbability >= RAIN_PROBABILITY_THRESHOLD,
        )
    }

    /** Резултат от търсенето → City. Невалидните резултати (без име/координати) се пропускат. */
    fun toCity(dto: GeocodingResultDto): City? {
        val name = dto.name ?: return null
        val latitude = dto.latitude ?: return null
        val longitude = dto.longitude ?: return null
        val description = listOfNotNull(dto.region?.takeIf { it != name }, dto.country)
            .joinToString(", ")
        return City(name, description, latitude, longitude)
    }
}
