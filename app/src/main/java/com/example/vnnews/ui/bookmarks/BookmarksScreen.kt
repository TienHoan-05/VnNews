package com.example.vnnews.ui.bookmarks

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.vnnews.data.local.ArticleEntity
import com.example.vnnews.data.remote.RssItemDto
import com.example.vnnews.ui.home.ArticleCard

@Composable
fun BookmarksScreen(
    onArticleClick: (url: String, title: String) -> Unit,
    onBrowseNews: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: BookmarksViewModel = viewModel(factory = BookmarksViewModel.Factory)
) {
    val bookmarks by viewModel.bookmarks.collectAsState()
    Box(modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
        if (bookmarks.isEmpty()) {
            Column(Modifier.fillMaxSize().padding(32.dp), verticalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterVertically),
                horizontalAlignment = Alignment.CenterHorizontally) {
                Box(Modifier.size(80.dp).background(MaterialTheme.colorScheme.primaryContainer, RoundedCornerShape(24.dp)),
                    contentAlignment = Alignment.Center) {
                    Icon(Icons.Default.BookmarkBorder, null, Modifier.size(32.dp), tint = MaterialTheme.colorScheme.primary)
                }
                Spacer(Modifier.height(8.dp))
                Text("Để dành một góc\ncho điều đáng đọc.", style = MaterialTheme.typography.headlineMedium,
                    textAlign = TextAlign.Center)
                Text("Lưu những bài bạn quan tâm để dễ tìm lại. Tiêu đề và mô tả vẫn xem được khi mất mạng.",
                    style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center)
                FilledTonalButton(onClick = onBrowseNews) { Text("Khám phá bản tin") }
            }
        } else {
            LazyColumn(Modifier.widthIn(max = 840.dp).fillMaxSize(), contentPadding = PaddingValues(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)) {
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text("Góc đọc của bạn", style = MaterialTheme.typography.headlineMedium)
                        Text("${bookmarks.size} câu chuyện đã lưu", style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
                items(bookmarks, key = { it.link }) { article ->
                    BookmarkItemCard(article, { onArticleClick(article.link, article.title) }, { viewModel.removeBookmark(article.link) })
                }
            }
        }
    }
}

@Composable
fun BookmarkItemCard(article: ArticleEntity, onClick: () -> Unit, onRemove: () -> Unit, modifier: Modifier = Modifier) {
    ArticleCard(
        article = RssItemDto(title = article.title, link = article.link, pubDate = article.pubDate,
            description = article.description, thumbnail = article.imageUrl),
        isBookmarked = true, onClick = onClick, onBookmarkClick = onRemove, modifier = modifier, showSummary = true
    )
}
