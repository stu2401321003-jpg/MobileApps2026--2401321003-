package com.example.whattowear_project.weather

import com.example.whattowear_project.weather.model.City
import com.example.whattowear_project.weather.model.Weather
import javax.inject.Inject

/**
 * Откъде идва времето. Интерфейс - за да можем в тестовете да подадем
 * фалшива имплементация, която работи без интернет.
 */
interface WeatherDataSource {
    suspend fun getWeather(city: City): Weather
    suspend fun searchCities(query: String): List<City>
}

/** Реалната имплементация - REST заявки към Open-Meteo през Retrofit. */
class RetrofitWeatherDataSource @Inject constructor(
    private val api: OpenMeteoApi,
) : WeatherDataSource {

    override suspend fun getWeather(city: City): Weather {
        val response = api.getForecast(latitude = city.latitude, longitude = city.longitude)
        return WeatherMapper.toWeather(response, city)
    }

    override suspend fun searchCities(query: String): List<City> =
        api.searchCity(name = query).results.orEmpty().mapNotNull(WeatherMapper::toCity)
}
