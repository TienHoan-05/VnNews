package com.example.vnnews.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_preferences")

class UserPreferencesRepository(private val dataStore: DataStore<Preferences>) {
    private companion object {
        val IS_DARK_THEME = booleanPreferencesKey("is_dark_theme")
        val SELECTED_CATEGORY = stringPreferencesKey("selected_category")
        val WEATHER_CITY = stringPreferencesKey("weather_city")
        val WEATHER_DEVICE_LOCATION = booleanPreferencesKey("weather_device_location")
    }

    val isDarkTheme: Flow<Boolean?> = dataStore.data
        .catch { exception ->
            if (exception is IOException) emit(emptyPreferences()) else throw exception
        }
        .map { preferences -> preferences[IS_DARK_THEME] }

    suspend fun saveDarkThemePreference(isDarkTheme: Boolean) {
        dataStore.edit { preferences ->
            preferences[IS_DARK_THEME] = isDarkTheme
        }
    }

    suspend fun saveDarkTheme(isDark: Boolean) = saveDarkThemePreference(isDark)

    val selectedCategory: Flow<String> = dataStore.data
        .catch { exception ->
            if (exception is IOException) emit(emptyPreferences()) else throw exception
        }
        .map { preferences -> preferences[SELECTED_CATEGORY] ?: "tin-moi-nhat" }

    suspend fun saveSelectedCategory(category: String) {
        dataStore.edit { preferences ->
            preferences[SELECTED_CATEGORY] = category
        }
    }

    val weatherCity: Flow<String> = dataStore.data
        .catch { if (it is IOException) emit(emptyPreferences()) else throw it }
        .map { it[WEATHER_CITY] ?: "hcm" }

    suspend fun saveWeatherCity(id: String) {
        dataStore.edit { it[WEATHER_CITY] = id }
    }

    val weatherDeviceLocation: Flow<Boolean> = dataStore.data
        .catch { if (it is IOException) emit(emptyPreferences()) else throw it }
        .map { it[WEATHER_DEVICE_LOCATION] ?: false }

    suspend fun saveWeatherDeviceLocation(enabled: Boolean) {
        dataStore.edit { it[WEATHER_DEVICE_LOCATION] = enabled }
    }
}
