package com.example.whattowear_project.data

import android.content.Context
import androidx.core.content.edit
import com.example.whattowear_project.weather.model.City
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Пази избрания град в SharedPreferences (малко хранилище за настройки).
 * Градът остава и след рестарт на приложението.
 */
@Singleton
class CityPreferences @Inject constructor(
    @ApplicationContext context: Context,
) {
    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    private val _city = MutableStateFlow(readCity())

    /** Текущият град (null = още не е избран). Екраните го „слушат“. */
    val city: StateFlow<City?> = _city.asStateFlow()

    fun saveCity(city: City) {
        prefs.edit {
            putString(KEY_NAME, city.name)
            putString(KEY_DESCRIPTION, city.description)
            // Double няма put метод - пазим го като битове в Long, за да няма загуба на точност.
            putLong(KEY_LATITUDE, city.latitude.toRawBits())
            putLong(KEY_LONGITUDE, city.longitude.toRawBits())
        }
        _city.value = city
    }

    private fun readCity(): City? {
        val name = prefs.getString(KEY_NAME, null) ?: return null
        if (!prefs.contains(KEY_LATITUDE) || !prefs.contains(KEY_LONGITUDE)) return null
        return City(
            name = name,
            description = prefs.getString(KEY_DESCRIPTION, null).orEmpty(),
            latitude = Double.fromBits(prefs.getLong(KEY_LATITUDE, 0L)),
            longitude = Double.fromBits(prefs.getLong(KEY_LONGITUDE, 0L)),
        )
    }

    private companion object {
        const val PREFS_NAME = "settings"
        const val KEY_NAME = "city_name"
        const val KEY_DESCRIPTION = "city_description"
        const val KEY_LATITUDE = "city_latitude"
        const val KEY_LONGITUDE = "city_longitude"
    }
}
