package com.example.vnnews.ui.home

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.Color
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.repeatOnLifecycle
import com.example.vnnews.data.repository.weatherCities
import kotlinx.coroutines.delay
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.math.roundToInt

@Composable
internal fun TodayPanel(state: WeatherUiState, viewModel: WeatherViewModel, compact: Boolean) {
    var now by remember { mutableStateOf(ZonedDateTime.now()) }
    var chooseCity by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val uriHandler = LocalUriHandler.current
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { result ->
        if (result.values.any { it }) viewModel.useCurrentLocation() else viewModel.locationPermissionDenied()
    }
    val useLocation = {
        val allowed = listOf(Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION)
            .any { ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED }
        if (allowed) viewModel.useCurrentLocation()
        else launcher.launch(arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION))
    }
    LaunchedEffect(lifecycle) {
        lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
            while (true) {
                now = ZonedDateTime.now()
                delay((60 - now.second) * 1000L)
            }
        }
    }
    val weather = state.weather
    val status = when {
        state.locating -> "Đang lấy vị trí…"
        state.loading && weather == null -> "Đang tải…"
        weather != null -> "${weather.temperature.roundToInt()}°C"
        else -> "Thời tiết"
    }
    Surface(onClick = { chooseCity = true }, shape = RoundedCornerShape(10.dp),
        color = Color.Transparent, border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)) {
        val fontScale = LocalDensity.current.fontScale
        BoxWithConstraints(Modifier.fillMaxWidth().padding(if (compact) 12.dp else 16.dp)) {
            val dateContent: @Composable () -> Unit = {
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(now.format(DateTimeFormatter.ofPattern(if (compact) "EEE, dd.MM · HH:mm" else "HH:mm", Locale.forLanguageTag("vi-VN"))),
                        style = if (compact) MaterialTheme.typography.labelMedium else MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.SemiBold)
                    if (!compact) Text(now.format(DateTimeFormatter.ofPattern("EEEE, dd/MM/yyyy", Locale.forLanguageTag("vi-VN"))),
                        style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            val weatherContent: @Composable () -> Unit = {
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        Icon(if (state.useDeviceLocation) Icons.Default.MyLocation else when (weather?.code) {
                            0, 1 -> Icons.Default.WbSunny
                            in 51..99 -> Icons.Default.Umbrella
                            else -> Icons.Default.Cloud
                        }, null, Modifier.size(14.dp), tint = MaterialTheme.colorScheme.primary)
                        Text(status, style = MaterialTheme.typography.labelLarge, maxLines = 1)
                        if (compact) Text(state.city.name, style = MaterialTheme.typography.labelSmall, maxLines = 1,
                            overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f, fill = false))
                        Icon(Icons.Default.ExpandMore, "Chọn vị trí thời tiết", Modifier.size(16.dp))
                    }
                    if (!compact) {
                        Text(state.city.name, style = MaterialTheme.typography.labelMedium)
                        Text(weather?.description ?: "Chạm để chọn khu vực", style = MaterialTheme.typography.labelSmall)
                    }
                    if (state.message != null || state.error) Text(
                        if (state.message != null) "Chạm để kiểm tra vị trí" else "Chạm để cập nhật thời tiết",
                        style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.error)
                }
            }
            if (maxWidth < 360.dp || fontScale >= 1.5f) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    dateContent()
                    weatherContent()
                }
            } else {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Box(Modifier.weight(1f)) { dateContent() }
                    Box(Modifier.weight(1.5f), contentAlignment = Alignment.CenterEnd) { weatherContent() }
                }
            }
        }
    }

    if (chooseCity) AlertDialog(
        onDismissRequest = { chooseCity = false }, title = { Text("Thời tiết khu vực") },
        text = {
            Column(Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("${state.city.name} · $status", style = MaterialTheme.typography.titleMedium)
                weather?.let {
                    Text(it.description + " · " + it.observedAt.atZone(ZoneId.systemDefault())
                        .format(DateTimeFormatter.ofPattern("dd/MM HH:mm")) + if (state.error) " · Dữ liệu cũ" else "",
                        style = MaterialTheme.typography.bodySmall)
                }
                if (state.loading) LinearProgressIndicator(Modifier.fillMaxWidth())
                state.message?.let { Text(it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error) }
                if (state.error) Text("Chưa cập nhật được thời tiết. Thử lại khi có mạng.", style = MaterialTheme.typography.bodySmall)
                FilledTonalButton(onClick = useLocation, enabled = !state.locating, modifier = Modifier.fillMaxWidth()) {
                    Icon(Icons.Default.MyLocation, null, Modifier.size(18.dp)); Spacer(Modifier.width(8.dp)); Text("Dùng vị trí của tôi")
                }
                Text("Cho phép vị trí gần đúng là đủ để xem thời tiết. Chỉ cập nhật khi bạn mở bản tin.", style = MaterialTheme.typography.bodySmall)
                Row {
                    TextButton(onClick = { context.startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)) }) { Text("Bật vị trí") }
                    TextButton(onClick = { context.startActivity(Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                        Uri.parse("package:${context.packageName}"))) }) { Text("Quyền ứng dụng") }
                }
                HorizontalDivider()
                Text("Hoặc chọn thành phố", style = MaterialTheme.typography.labelLarge)
                weatherCities.forEach { city ->
                    Row(Modifier.fillMaxWidth().clickable { viewModel.selectCity(city); chooseCity = false },
                        verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(selected = !state.useDeviceLocation && state.city.id == city.id,
                            onClick = { viewModel.selectCity(city); chooseCity = false })
                        Text(city.name, style = MaterialTheme.typography.bodyMedium)
                    }
                }
                Text("Dữ liệu Open-Meteo · CC BY 4.0", style = MaterialTheme.typography.labelSmall,
                    modifier = Modifier.clickable { uriHandler.openUri("https://open-meteo.com/") })
            }
        },
        confirmButton = { TextButton(onClick = { chooseCity = false }) { Text("Đóng") } },
        dismissButton = { TextButton(onClick = viewModel::refresh) { Text("Cập nhật") } }
    )
}
