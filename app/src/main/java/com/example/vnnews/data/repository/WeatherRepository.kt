package com.example.vnnews.data.repository

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.io.IOException
import java.time.Instant

data class WeatherCity(val id: String, val name: String, val latitude: Double, val longitude: Double)

val weatherCities = listOf(
    WeatherCity("hcm", "TP. Hồ Chí Minh", 10.8231, 106.6297),
    WeatherCity("hanoi", "Hà Nội", 21.0285, 105.8542),
    WeatherCity("danang", "Đà Nẵng", 16.0544, 108.2022),
    WeatherCity("hue", "Huế", 16.4637, 107.5909),
    WeatherCity("cantho", "Cần Thơ", 10.0452, 105.7469),
    WeatherCity("haiphong", "Hải Phòng", 20.8449, 106.6881),
    WeatherCity("nhatrang", "Nha Trang", 12.2388, 109.1967),
    WeatherCity("dalat", "Đà Lạt", 11.9404, 108.4583)
)

data class LocalWeather(val temperature: Double, val code: Int, val observedAt: Instant) {
    val description: String get() = when (code) {
        0 -> "Trời quang"
        1, 2 -> "Ít mây"
        3 -> "Nhiều mây"
        45, 48 -> "Sương mù"
        in 51..57 -> "Mưa phùn"
        in 61..67, in 80..82 -> "Có mưa"
        in 71..77, 85, 86 -> "Có tuyết"
        in 95..99 -> "Mưa dông"
        else -> "Thời tiết hiện tại"
    }
}

interface WeatherRepository {
    suspend fun getWeather(city: WeatherCity): LocalWeather
}

class OpenMeteoWeatherRepository(private val client: OkHttpClient) : WeatherRepository {
    override suspend fun getWeather(city: WeatherCity): LocalWeather = withContext(Dispatchers.IO) {
        val url = "https://api.open-meteo.com/v1/forecast?latitude=${city.latitude}&longitude=${city.longitude}" +
            "&current=temperature_2m,weather_code&timeformat=unixtime&timezone=Asia%2FHo_Chi_Minh"
        client.newCall(Request.Builder().url(url).build()).execute().use { response ->
            if (!response.isSuccessful) throw IOException("Không tải được thời tiết")
            val current = JSONObject(response.body?.string() ?: throw IOException("Thiếu dữ liệu"))
                .getJSONObject("current")
            LocalWeather(current.getDouble("temperature_2m"), current.getInt("weather_code"),
                Instant.ofEpochSecond(current.getLong("time")))
        }
    }
}
