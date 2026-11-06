package com.example.vnnews.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.vnnews.NewsApplication
import com.example.vnnews.data.local.UserPreferencesRepository
import com.example.vnnews.data.repository.LocalWeather
import com.example.vnnews.data.repository.WeatherCity
import com.example.vnnews.data.repository.WeatherRepository
import com.example.vnnews.data.repository.weatherCities
import com.example.vnnews.data.repository.DeviceLocationProvider
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

data class WeatherUiState(
    val city: WeatherCity = weatherCities.first(),
    val weather: LocalWeather? = null,
    val loading: Boolean = true,
    val error: Boolean = false,
    val useDeviceLocation: Boolean = false,
    val locating: Boolean = false,
    val message: String? = null
)

class WeatherViewModel(
    private val repository: WeatherRepository,
    private val preferences: UserPreferencesRepository,
    private val locationProvider: DeviceLocationProvider? = null
) : ViewModel() {
    private val _uiState = MutableStateFlow(WeatherUiState())
    val uiState = _uiState.asStateFlow()
    private var job: Job? = null
    private var initialized = false

    init {
        viewModelScope.launch {
            val saved = preferences.weatherCity.first()
            val automatic = preferences.weatherDeviceLocation.first()
            initialized = true
            _uiState.value = WeatherUiState(city = weatherCities.find { it.id == saved } ?: weatherCities.first())
            if (automatic && locationProvider != null) useCurrentLocation() else selectCity(_uiState.value.city)
        }
    }

    fun selectCity(city: WeatherCity) {
        job?.cancel()
        _uiState.value = WeatherUiState(city = city)
        job = viewModelScope.launch {
            try { preferences.saveWeatherCity(city.id); preferences.saveWeatherDeviceLocation(false) }
            catch (e: CancellationException) { throw e }
            catch (_: Exception) { /* Weather remains usable if preference storage is unavailable. */ }
            fetch(city)
        }
    }

    fun refresh() {
        if (!initialized || job?.isActive == true) return
        if (_uiState.value.useDeviceLocation) useCurrentLocation()
        else job = viewModelScope.launch { fetch(_uiState.value.city) }
    }

    fun useCurrentLocation() {
        job?.cancel()
        _uiState.value = _uiState.value.copy(useDeviceLocation = true, locating = true, loading = true, message = null)
        job = viewModelScope.launch {
            try {
                val city = locationProvider?.currentArea() ?: error("Chưa có dịch vụ vị trí.")
                try { preferences.saveWeatherDeviceLocation(true) }
                catch (e: CancellationException) { throw e }
                catch (_: Exception) { }
                val sameArea = _uiState.value.city == city
                _uiState.value = _uiState.value.copy(city = city, weather = if (sameArea) _uiState.value.weather else null, locating = false)
                fetch(city)
            } catch (e: CancellationException) { throw e
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(loading = false, locating = false,
                    message = e.message ?: "Chưa lấy được vị trí. Bạn có thể chọn thành phố.")
            }
        }
    }

    fun locationPermissionDenied() {
        _uiState.value = _uiState.value.copy(message = "Chưa được cấp quyền vị trí. Chọn thành phố hoặc mở cài đặt ứng dụng để cấp quyền.")
    }

    fun pauseLocationLookup() {
        if (_uiState.value.locating) {
            job?.cancel()
            _uiState.value = _uiState.value.copy(loading = false, locating = false)
        }
    }

    private suspend fun fetch(city: WeatherCity) {
        _uiState.value = _uiState.value.copy(loading = true, error = false, message = null)
        try {
            _uiState.value = _uiState.value.copy(city = city, weather = repository.getWeather(city), loading = false, error = false)
        } catch (e: CancellationException) { throw e
        } catch (_: Exception) {
            _uiState.value = _uiState.value.copy(loading = false, error = true)
        }
    }

    companion object {
        val Factory = viewModelFactory {
            initializer {
                val app = this[APPLICATION_KEY] as NewsApplication
                WeatherViewModel(app.container.weatherRepository, app.container.userPreferencesRepository, app.container.deviceLocationProvider)
            }
        }
    }
}
