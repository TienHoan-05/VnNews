package com.example.vnnews.ui.home

import androidx.compose.foundation.clickable
import androidx.compose.foundation.background
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.platform.testTag
import androidx.compose.foundation.lazy.LazyListState
import kotlinx.coroutines.launch
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.repeatOnLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.vnnews.data.remote.RssItemDto
import kotlinx.coroutines.delay
import java.time.format.DateTimeFormatter

@Composable
private fun isCompactNewsWindow(): Boolean {
    val width = LocalWindowInfo.current.containerSize.width
    return with(LocalDensity.current) { width.toDp() } < 600.dp
}

@Composable
fun HomeScreen(
    onArticleClick: (url: String, title: String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel = viewModel(factory = HomeViewModel.Factory),
    weatherViewModel: WeatherViewModel = viewModel(factory = WeatherViewModel.Factory),
    listState: LazyListState = rememberLazyListState()
) {
    val uiState by viewModel.uiState.collectAsState()
    val source by viewModel.selectedSource.collectAsState()
    val selectedCategory by viewModel.selectedCategory.collectAsState()
    val weather by weatherViewModel.uiState.collectAsState()
    var query by rememberSaveable { mutableStateOf("") }
    var sourceMenu by remember { mutableStateOf(false) }
    var displayMenu by remember { mutableStateOf(false) }
    var searchExpanded by rememberSaveable { mutableStateOf(false) }
    var dense by rememberSaveable { mutableStateOf(false) }
    val compact = isCompactNewsWindow()
    val scope = rememberCoroutineScope()
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    val success = uiState as? HomeUiState.Success
    val filteredArticles = remember(success?.articles, query) { success?.articles.orEmpty().filter {
        query.isBlank() || it.displayTitle.contains(query.trim(), true) ||
            it.getCleanDescription().contains(query.trim(), true) || it.publisher().contains(query.trim(), true)
    } }
    val category = selectedCategory.id
    LaunchedEffect(lifecycle) {
        lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
            try {
                while (true) {
                    weatherViewModel.refresh()
                    delay(15 * 60 * 1000L)
                }
            } finally { weatherViewModel.pauseLocationLookup() }
        }
    }

    Box(modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
        LazyColumn(
            state = listState,
            modifier = Modifier.widthIn(max = 840.dp).fillMaxSize().testTag("news-list"),
            contentPadding = PaddingValues(start = 20.dp, end = 20.dp, bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item(key = "today", contentType = "weather") { TodayPanel(weather, weatherViewModel, compact = true) }
            item(key = "categories") {
                LazyRow(modifier = Modifier.selectableGroup(), horizontalArrangement = Arrangement.spacedBy(24.dp)) {
                    items(viewModel.categoriesList, key = { it.id }) { item ->
                        val selected = item.id == category
                        Column(Modifier.selectable(selected = selected, role = Role.Tab, onClick = {
                                viewModel.selectCategory(item)
                                scope.launch { listState.scrollToItem(0) }
                            }).heightIn(min = 48.dp).widthIn(min = 48.dp), verticalArrangement = Arrangement.SpaceBetween) {
                            Text(item.title, modifier = Modifier.padding(top = 7.dp), style = MaterialTheme.typography.bodyMedium,
                                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                                color = if (selected) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant)
                            Box(Modifier.width(if (selected) 24.dp else 0.dp).height(3.dp)
                                .background(MaterialTheme.colorScheme.primary, RoundedCornerShape(2.dp)))
                        }
                    }
                }
            }
            if (searchExpanded || query.isNotEmpty()) item(key = "search", contentType = "search") {
                OutlinedTextField(
                    value = query, onValueChange = { query = it; scope.launch { listState.scrollToItem(0) } },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Tìm trong bản tin") },
                    placeholder = { Text("Tìm trong bản tin đang xem…", style = MaterialTheme.typography.bodyMedium) },
                    leadingIcon = { Icon(Icons.Default.Search, null) },
                    trailingIcon = {
                        if (query.isNotEmpty()) IconButton(onClick = { query = "" }) {
                            Icon(Icons.Default.Close, "Xóa tìm kiếm")
                        }
                    },
                    singleLine = true, shape = RoundedCornerShape(16.dp)
                )
            }
            item(key = "sources") {
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Box(Modifier.weight(1f)) {
                        Column(Modifier.clickable(role = Role.Button, onClickLabel = "Chọn nguồn báo") { sourceMenu = true }
                            .heightIn(min = 48.dp).padding(vertical = 4.dp)) {
                            Text(if (query.isNotBlank()) "Kết quả tìm kiếm" else selectedCategory.title.let {
                                if (it == "Mới nhất") "Hôm nay" else it
                            }, modifier = Modifier.semantics { heading() }, style = MaterialTheme.typography.headlineMedium, fontFamily = FontFamily.Serif,
                                fontWeight = FontWeight.Bold)
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text((viewModel.sources.find { it.id == source }?.title ?: "Tất cả nguồn") +
                                    (success?.let { " · ${filteredArticles.size} bài" } ?: ""),
                                    style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Icon(Icons.Default.ExpandMore, null, Modifier.size(15.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                        DropdownMenu(expanded = sourceMenu, onDismissRequest = { sourceMenu = false }) {
                            DropdownMenuItem(text = { Text("Tất cả nguồn") }, onClick = {
                                sourceMenu = false; viewModel.selectSource(null); scope.launch { listState.scrollToItem(0) }
                            }, trailingIcon = { if (source == null) Icon(Icons.Default.Check, null) })
                            viewModel.sources.forEach { item ->
                                DropdownMenuItem(text = { Text(item.title) }, onClick = {
                                    sourceMenu = false; viewModel.selectSource(item.id); scope.launch { listState.scrollToItem(0) }
                                }, trailingIcon = { if (source == item.id) Icon(Icons.Default.Check, null) })
                            }
                        }
                    }
                    IconButton(onClick = { searchExpanded = !searchExpanded }) { Icon(Icons.Default.Search, "Tìm tin") }
                    Box {
                        IconButton(onClick = { displayMenu = true }) { Icon(Icons.Default.MoreHoriz, "Tùy chọn bản tin") }
                        DropdownMenu(expanded = displayMenu, onDismissRequest = { displayMenu = false }) {
                            DropdownMenuItem(text = { Text(if (dense) "Hiện ảnh bản tin" else "Đọc lướt không ảnh") },
                                leadingIcon = { Icon(Icons.Default.ViewHeadline, null) },
                                onClick = { dense = !dense; displayMenu = false })
                            DropdownMenuItem(text = { Text("Làm mới bản tin") }, leadingIcon = { Icon(Icons.Default.Refresh, null) },
                                onClick = { viewModel.retry(); displayMenu = false })
                        }
                    }
                }
            }
            when (val state = uiState) {
                HomeUiState.Loading -> item {
                    Box(Modifier.fillMaxWidth().height(160.dp), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
                }
                is HomeUiState.Error -> item {
                    Column(Modifier.fillMaxWidth().padding(vertical = 32.dp), horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Icon(Icons.Default.CloudOff, null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(state.message, style = MaterialTheme.typography.bodyMedium)
                        FilledTonalButton(onClick = viewModel::retry) { Text("Tải lại bản tin") }
                    }
                }
                is HomeUiState.Success -> {
                    if (state.unavailableSources.isNotEmpty()) item {
                        Text("Chưa tải được ${state.unavailableSources.joinToString()}. Bạn có thể thử làm mới.",
                            style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    val articles = filteredArticles
                    if (articles.isEmpty()) item {
                        Text(if (query.isBlank()) "Chưa có tin trong chuyên mục này." else "Không tìm thấy bài phù hợp. Thử từ khóa khác nhé.",
                            modifier = Modifier.padding(vertical = 24.dp), color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    items(articles, key = { it.link }, contentType = {
                        if (!dense && it == articles.first() && query.isBlank()) "featured" else "article"
                    }) { article ->
                        if (!dense && query.isBlank() && article == articles.getOrNull(1)) {
                            Row(Modifier.fillMaxWidth().padding(top = 12.dp, bottom = 6.dp), verticalAlignment = Alignment.CenterVertically) {
                                Box(Modifier.width(3.dp).height(14.dp).background(MaterialTheme.colorScheme.primary))
                                Text("DÒNG TIN MỚI", modifier = Modifier.padding(start = 8.dp),
                                    style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.5.sp)
                                Spacer(Modifier.weight(1f))
                                Text("${articles.size - 1} bài", style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                        ArticleCard(
                            article, article.link in state.bookmarkedUrls,
                            onClick = { onArticleClick(article.link, article.displayTitle) },
                            onBookmarkClick = { viewModel.toggleBookmark(article) },
                            featured = !dense && article == articles.first() && query.isBlank(),
                            compact = compact, showImage = !dense
                        )
                    }
                    if (articles.isNotEmpty()) item {
                        Column(Modifier.fillMaxWidth().padding(vertical = 24.dp), horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Icon(Icons.Default.CheckCircleOutline, null, Modifier.size(22.dp), tint = MaterialTheme.colorScheme.primary)
                            Text("Bạn đã bắt kịp bản tin", fontFamily = FontFamily.Serif, style = MaterialTheme.typography.titleMedium)
                            Text("Hẹn bạn ở những câu chuyện tiếp theo.", style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ArticleCard(
    article: RssItemDto,
    isBookmarked: Boolean,
    onClick: () -> Unit,
    onBookmarkClick: () -> Unit,
    modifier: Modifier = Modifier,
    featured: Boolean = false,
    compact: Boolean = isCompactNewsWindow(),
    showImage: Boolean = true,
    showSummary: Boolean = false
) {
    val fontScale = LocalDensity.current.fontScale
    if (featured) {
        Surface(modifier.fillMaxWidth().clickable(onClick = onClick), shape = RoundedCornerShape(18.dp),
            color = MaterialTheme.colorScheme.surface,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.65f))) {
            BoxWithConstraints {
                val hasImage = showImage && !article.getImageUrl().isNullOrBlank()
                if (maxWidth >= 600.dp && fontScale < 1.5f && hasImage) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        ArticleImage(article, Modifier.weight(1f).height(230.dp), rounded = false)
                        HeroBody(article, isBookmarked, onBookmarkClick, Modifier.weight(1f), compact = false)
                    }
                } else {
                    Column {
                        if (hasImage) ArticleImage(article, Modifier.fillMaxWidth().height(if (compact) 168.dp else 220.dp), rounded = false)
                        HeroBody(article, isBookmarked, onBookmarkClick, Modifier.fillMaxWidth(), compact)
                    }
                }
            }
        }
    } else {
        Column(modifier.fillMaxWidth()) {
            BoxWithConstraints(Modifier.fillMaxWidth().clickable(onClickLabel = "Đọc bài viết", onClick = onClick).padding(vertical = 12.dp)) {
                val thumbnail = showImage && maxWidth >= 300.dp && fontScale < 1.5f && !article.getImageUrl().isNullOrBlank()
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(horizontalArrangement = Arrangement.spacedBy(16.dp), verticalAlignment = Alignment.Top) {
                        StableArticleText(article.displayTitle, MaterialTheme.typography.titleMedium,
                            maxLines = if (fontScale >= 1.5f) 6 else 4, modifier = Modifier.weight(1f))
                        if (thumbnail) ArticleImage(article,
                            Modifier.width(if (compact) 88.dp else 112.dp).aspectRatio(1.2f))
                    }
                    if (showSummary && article.getCleanDescription().isNotBlank()) {
                        StableArticleText(article.getCleanDescription(), MaterialTheme.typography.bodyMedium, maxLines = 3,
                            modifier = Modifier.fillMaxWidth(), color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    ArticleMeta(article, isBookmarked, onBookmarkClick)
                }
            }
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.75f))
        }
    }
}

@Composable
private fun HeroBody(article: RssItemDto, saved: Boolean, onBookmark: () -> Unit, modifier: Modifier, compact: Boolean) {
    Column(modifier.padding(horizontal = 16.dp, vertical = 12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("MỚI CẬP NHẬT", style = MaterialTheme.typography.labelSmall, letterSpacing = 1.5.sp,
            fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.tertiary)
        StableArticleText(article.displayTitle, MaterialTheme.typography.headlineSmall, maxLines = 6,
            modifier = Modifier.fillMaxWidth().semantics { heading() })
        if (!compact && article.getCleanDescription().isNotBlank()) {
            StableArticleText(article.getCleanDescription(), MaterialTheme.typography.bodyMedium, maxLines = 2,
                modifier = Modifier.fillMaxWidth(), color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        ArticleMeta(article, saved, onBookmark)
    }
}

@Composable
private fun ArticleMeta(article: RssItemDto, saved: Boolean, onBookmark: () -> Unit) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(5.dp)) {
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Box(Modifier.size(5.dp).background(publisherColor(article.publisher()), RoundedCornerShape(50)))
                Text(article.publisher(), style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f, fill = false))
            }
            val date = article.publishedAt()?.atZone(java.time.ZoneId.systemDefault())?.let {
                it.format(DateTimeFormatter.ofPattern(if (it.toLocalDate() == java.time.LocalDate.now()) "HH:mm" else "dd/MM · HH:mm"))
            } ?: article.pubDate.orEmpty().take(16)
            if (date.isNotBlank()) Text(date, style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        IconToggleButton(checked = saved, onCheckedChange = { onBookmark() }, modifier = Modifier.size(48.dp)) {
            Icon(if (saved) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                if (saved) "Bỏ lưu" else "Lưu đọc sau", Modifier.size(18.dp),
                tint = if (saved) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

private fun publisherColor(publisher: String): Color = when (publisher) {
    "Tuổi Trẻ" -> Color(0xFFCA3C35)
    "Thanh Niên" -> Color(0xFF397BC0)
    "Dân trí" -> Color(0xFF2D8B5B)
    "VnExpress" -> Color(0xFF9F224E)
    else -> Color(0xFF73877D)
}

@Composable
private fun ArticleImage(article: RssItemDto, modifier: Modifier, rounded: Boolean = true) {
    AsyncImage(model = ImageRequest.Builder(LocalContext.current).data(article.getImageUrl()).crossfade(true).build(),
        contentDescription = null, contentScale = ContentScale.Crop,
        modifier = (if (rounded) modifier.clip(RoundedCornerShape(10.dp)) else modifier)
            .background(MaterialTheme.colorScheme.surfaceContainerLow))
}
