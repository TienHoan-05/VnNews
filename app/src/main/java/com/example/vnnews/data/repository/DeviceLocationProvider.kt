package com.example.vnnews.data.repository

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.os.CancellationSignal
import androidx.core.content.ContextCompat
import androidx.core.location.LocationManagerCompat
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withTimeoutOrNull
import java.io.IOException
import kotlin.coroutines.resume

interface DeviceLocationProvider {
    suspend fun currentArea(): WeatherCity
}

class AndroidDeviceLocationProvider(private val context: Context) : DeviceLocationProvider {
    @SuppressLint("MissingPermission")
    override suspend fun currentArea(): WeatherCity {
        val coarse = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
        val fine = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
        if (!coarse && !fine) throw SecurityException("Hãy cấp quyền vị trí hoặc chọn thành phố.")
        val manager = context.getSystemService(LocationManager::class.java)
        if (!LocationManagerCompat.isLocationEnabled(manager)) throw IOException("Hãy bật vị trí trên thiết bị rồi thử lại.")
        val providers = listOf("fused", LocationManager.NETWORK_PROVIDER, LocationManager.GPS_PROVIDER)
            .filter { (it != LocationManager.GPS_PROVIDER || fine) && it in manager.getProviders(true) }
        if (providers.isEmpty()) throw IOException("Chưa có nguồn vị trí. Bạn có thể chọn thành phố.")
        val location = withTimeoutOrNull(15_000L) {
            coroutineScope {
                val result = CompletableDeferred<Location?>()
                var remaining = providers.size
                val requests = providers.map { provider -> launch {
                    val fix = suspendCancellableCoroutine<Location?> { continuation ->
                        val signal = CancellationSignal()
                        continuation.invokeOnCancellation { signal.cancel() }
                        try {
                            LocationManagerCompat.getCurrentLocation(manager, provider, signal,
                                ContextCompat.getMainExecutor(context)) { value ->
                                if (continuation.isActive) continuation.resume(value)
                            }
                        } catch (_: Exception) {
                            if (continuation.isActive) continuation.resume(null)
                        }
                    }
                    if (fix != null) result.complete(fix)
                    remaining--
                    if (remaining == 0) result.complete(null)
                } }
                try { result.await() } finally { requests.forEach { it.cancel() } }
            }
        } ?: throw IOException("Chưa lấy được vị trí. Thử lại ngoài trời hoặc chọn thành phố.")
        // Coordinates stay in memory and are used directly, never rounded to a preset city.
        return WeatherCity("device", "Vị trí hiện tại", location.latitude, location.longitude)
    }
}
