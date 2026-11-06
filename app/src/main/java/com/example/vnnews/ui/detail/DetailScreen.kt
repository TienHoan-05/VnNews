package com.example.vnnews.ui.detail

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Box
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.OpenInBrowser
import androidx.compose.material.icons.filled.TextFields
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import android.content.Intent
import android.net.Uri

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailScreen(
    url: String,
    title: String,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val density = LocalDensity.current
    var textZoom by rememberSaveable(url) { mutableIntStateOf(100) }
    var textMenu by remember { mutableStateOf(false) }
    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                expandedHeight = maxOf(64.dp, with(density) { 42.sp.toDp() } + 16.dp),
                title = {
                    Text(
                        text = title.ifBlank { "VnNews" },
                        style = MaterialTheme.typography.bodyMedium,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                actions = {
                    Box {
                        IconButton(onClick = { textMenu = true }) {
                            Icon(Icons.Default.TextFields, "Cỡ chữ bài viết: $textZoom phần trăm")
                        }
                        DropdownMenu(expanded = textMenu, onDismissRequest = { textMenu = false }) {
                            listOf(100, 115, 130, 150).forEach { zoom ->
                                DropdownMenuItem(
                                    text = { Text("Cỡ chữ $zoom%") },
                                    trailingIcon = { if (textZoom == zoom) Icon(Icons.Default.Check, null) },
                                    onClick = { textZoom = zoom; textMenu = false }
                                )
                            }
                        }
                    }
                    IconButton(onClick = {
                        context.startActivity(Intent.createChooser(Intent(Intent.ACTION_SEND).apply {
                            type = "text/plain"
                            putExtra(Intent.EXTRA_TEXT, "$title\n$url")
                        }, "Chia sẻ bài viết"))
                    }) { Icon(Icons.Default.Share, "Chia sẻ bài viết") }
                    IconButton(onClick = { context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url))) }) {
                        Icon(Icons.Default.OpenInBrowser, "Mở bằng trình duyệt")
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Quay lại"
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        ArticleWebView(
            url = url,
            textZoom = textZoom,
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        )
    }
}
