package com.example.whattowear_project.weather.model

/** Град, избран от потребителя (от търсенето в Open-Meteo). */
data class City(
    val name: String,
    /** Напр. „Пловдив, България“ - за да различим едноименни градове. */
    val description: String,
    val latitude: Double,
    val longitude: Double,
)

/**
 * Състоянието на времето - превърнато от WMO кода на Open-Meteo в нещо разбираемо.
 * WMO кодове: https://open-meteo.com/en/docs (раздел „Weather variable documentation“).
 */
enum class WeatherCondition(val isRainy: Boolean = false) {
    CLEAR,
    PARTLY_CLOUDY,
    CLOUDY,
    FOG,
    DRIZZLE(isRainy = true),
    RAIN(isRainy = true),
    SNOW,
    THUNDERSTORM(isRainy = true),
    ;

    companion object {
        fun fromWmoCode(code: Int?): WeatherCondition = when (code) {
            0 -> CLEAR
            1, 2 -> PARTLY_CLOUDY
            3 -> CLOUDY
            45, 48 -> FOG
            51, 53, 55, 56, 57 -> DRIZZLE
            61, 63, 65, 66, 67, 80, 81, 82 -> RAIN
            71, 73, 75, 77, 85, 86 -> SNOW
            95, 96, 99 -> THUNDERSTORM
            else -> CLOUDY // непознат код - най-неутралният вариант
        }
    }
}

/** Времето за днес в избрания град - всичко, което ни трябва за екрана и за правилата. */
data class Weather(
    val city: City,
    /** Температура в момента (°C), закръглена. */
    val temperature: Int,
    /** „Усеща се като“ (°C). */
    val feelsLike: Int,
    val minTemperature: Int,
    val maxTemperature: Int,
    /** Шанс за валежи днес (0-100 %). */
    val precipitationProbability: Int,
    val windSpeed: Int,
    val condition: WeatherCondition,
    val isDay: Boolean,
    /** Ще вали ли днес - ползва се от правилата (чадър, непромокаеми дрехи). */
    val isRainy: Boolean,
)
