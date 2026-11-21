package com.example.whattowear_project.data.repository

import com.example.whattowear_project.data.CityPreferences
import com.example.whattowear_project.weather.WeatherDataSource
import com.example.whattowear_project.weather.model.City
import com.example.whattowear_project.weather.model.Weather
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repository за времето: единственото място, откъдето екраните взимат
 * времето, търсят градове и сменят избрания град.
 */
@Singleton
class WeatherRepository @Inject constructor(
    private val dataSource: WeatherDataSource,
    private val cityPreferences: CityPreferences,
) {
    /** Избраният град (null = още не е избран). */
    val selectedCity: StateFlow<City?> = cityPreferences.city

    fun selectCity(city: City) = cityPreferences.saveCity(city)

    suspend fun getWeather(city: City): Weather = dataSource.getWeather(city)

    suspend fun searchCities(query: String): List<City> = dataSource.searchCities(query)
}
