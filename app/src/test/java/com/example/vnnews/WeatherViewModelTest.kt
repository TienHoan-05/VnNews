package com.example.vnnews

import com.example.vnnews.data.local.UserPreferencesRepository
import com.example.vnnews.data.repository.*
import com.example.vnnews.fake.FakeDataStore
import com.example.vnnews.ui.home.WeatherViewModel
import com.example.vnnews.util.MainDispatcherRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import java.io.IOException
import java.time.Instant

@OptIn(ExperimentalCoroutinesApi::class)
class WeatherViewModelTest {
    @get:Rule val dispatcher = MainDispatcherRule()
    private val sample = LocalWeather(28.5, 3, Instant.parse("2026-11-06T08:00:00Z"))

    @Test fun savedCity_isRestoredAndUsedForWeather() = runTest {
        val preferences = UserPreferencesRepository(FakeDataStore())
        preferences.saveWeatherCity("hanoi")
        var requested = ""
        val vm = WeatherViewModel(object : WeatherRepository {
            override suspend fun getWeather(city: WeatherCity): LocalWeather { requested = city.id; return sample }
        }, preferences)
        advanceUntilIdle()
        assertEquals("hanoi", requested)
        assertEquals(sample, vm.uiState.value.weather)
        assertFalse(vm.uiState.value.loading)
    }

    @Test fun failedRefresh_retainsPreviousWeatherAndCanRecover() = runTest {
        var offline = false
        val vm = WeatherViewModel(object : WeatherRepository {
            override suspend fun getWeather(city: WeatherCity): LocalWeather {
                if (offline) throw IOException("Offline")
                return sample
            }
        }, UserPreferencesRepository(FakeDataStore()))
        advanceUntilIdle()
        offline = true
        vm.refresh()
        advanceUntilIdle()
        assertTrue(vm.uiState.value.error)
        assertEquals(sample, vm.uiState.value.weather)
        offline = false
        vm.refresh()
        advanceUntilIdle()
        assertFalse(vm.uiState.value.error)
    }

    @Test fun switchingCity_persistsSelectionAndCancelsPreviousRequest() = runTest {
        val preferences = UserPreferencesRepository(FakeDataStore())
        val vm = WeatherViewModel(object : WeatherRepository {
            override suspend fun getWeather(city: WeatherCity): LocalWeather {
                delay(if (city.id == "hcm") 1000 else 100)
                return sample.copy(temperature = if (city.id == "hanoi") 22.0 else 30.0)
            }
        }, preferences)
        vm.selectCity(weatherCities.first { it.id == "hanoi" })
        advanceUntilIdle()
        assertEquals("hanoi", vm.uiState.value.city.id)
        assertEquals("hanoi", preferences.weatherCity.first())
        assertEquals(22.0, vm.uiState.value.weather!!.temperature, 0.0)
    }

    @Test fun deviceLocation_usesActualCoordinatesAndPersistsAutomaticMode() = runTest {
        val preferences = UserPreferencesRepository(FakeDataStore())
        val area = WeatherCity("device", "Vị trí hiện tại", 20.1, 105.2)
        var requested: WeatherCity? = null
        val vm = WeatherViewModel(object : WeatherRepository {
            override suspend fun getWeather(city: WeatherCity): LocalWeather { requested = city; return sample }
        }, preferences, object : DeviceLocationProvider {
            override suspend fun currentArea() = area
        })
        vm.useCurrentLocation()
        advanceUntilIdle()
        assertEquals(area, requested)
        assertTrue(vm.uiState.value.useDeviceLocation)
        assertTrue(preferences.weatherDeviceLocation.first())
        vm.selectCity(weatherCities.first { it.id == "hanoi" })
        advanceUntilIdle()
        assertFalse(preferences.weatherDeviceLocation.first())
        assertFalse(vm.uiState.value.useDeviceLocation)
    }

    @Test fun locationPermissionRevoked_keepsExistingWeatherAndProvidesRecoveryMessage() = runTest {
        val vm = WeatherViewModel(object : WeatherRepository {
            override suspend fun getWeather(city: WeatherCity) = sample
        }, UserPreferencesRepository(FakeDataStore()), object : DeviceLocationProvider {
            override suspend fun currentArea(): WeatherCity = throw SecurityException("Cần cấp quyền vị trí")
        })
        advanceUntilIdle()
        vm.useCurrentLocation()
        advanceUntilIdle()
        assertEquals(sample, vm.uiState.value.weather)
        assertFalse(vm.uiState.value.locating)
        assertFalse(vm.uiState.value.loading)
        assertTrue(vm.uiState.value.message!!.contains("quyền"))
    }

    @Test fun leavingHome_cancelsPendingLocationSoLateFixCannotChangeManualCity() = runTest {
        val vm = WeatherViewModel(object : WeatherRepository {
            override suspend fun getWeather(city: WeatherCity) = sample
        }, UserPreferencesRepository(FakeDataStore()), object : DeviceLocationProvider {
            override suspend fun currentArea(): WeatherCity {
                delay(10_000)
                return WeatherCity("device", "Vị trí hiện tại", 1.0, 2.0)
            }
        })
        vm.useCurrentLocation()
        vm.pauseLocationLookup()
        vm.selectCity(weatherCities.first { it.id == "dalat" })
        advanceUntilIdle()
        assertEquals("dalat", vm.uiState.value.city.id)
        assertFalse(vm.uiState.value.locating)
    }
}
