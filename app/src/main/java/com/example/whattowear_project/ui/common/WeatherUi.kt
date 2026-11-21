package com.example.whattowear_project.ui.common

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.example.whattowear_project.R
import com.example.whattowear_project.weather.model.Weather
import com.example.whattowear_project.weather.model.WeatherCondition

/** Как изглежда времето на екрана: текст, икона и фон на картата. */

@get:StringRes
val WeatherCondition.labelRes: Int
    get() = when (this) {
        WeatherCondition.CLEAR -> R.string.weather_clear
        WeatherCondition.PARTLY_CLOUDY -> R.string.weather_partly_cloudy
        WeatherCondition.CLOUDY -> R.string.weather_cloudy
        WeatherCondition.FOG -> R.string.weather_fog
        WeatherCondition.DRIZZLE -> R.string.weather_drizzle
        WeatherCondition.RAIN -> R.string.weather_rain
        WeatherCondition.SNOW -> R.string.weather_snow
        WeatherCondition.THUNDERSTORM -> R.string.weather_thunderstorm
    }

/** Цветна икона - различна денем и нощем за ясно/разкъсана облачност. */
@DrawableRes
fun WeatherCondition.iconRes(isDay: Boolean): Int = when (this) {
    WeatherCondition.CLEAR -> if (isDay) R.drawable.ic_weather_clear_day else R.drawable.ic_weather_clear_night
    WeatherCondition.PARTLY_CLOUDY ->
        if (isDay) R.drawable.ic_weather_partly_day else R.drawable.ic_weather_partly_night
    WeatherCondition.CLOUDY -> R.drawable.ic_weather_cloudy
    WeatherCondition.FOG -> R.drawable.ic_weather_fog
    WeatherCondition.DRIZZLE, WeatherCondition.RAIN -> R.drawable.ic_weather_rain
    WeatherCondition.SNOW -> R.drawable.ic_weather_snow
    WeatherCondition.THUNDERSTORM -> R.drawable.ic_weather_thunder
}

/** Фонът на голямата карта - като небето навън. */
@get:DrawableRes
val Weather.heroBackgroundRes: Int
    get() = when {
        !isDay -> R.drawable.bg_hero_night
        condition.isRainy -> R.drawable.bg_hero_rain
        condition == WeatherCondition.SNOW -> R.drawable.bg_hero_snow
        condition == WeatherCondition.CLOUDY || condition == WeatherCondition.FOG -> R.drawable.bg_hero_cloudy
        else -> R.drawable.bg_hero_day
    }
