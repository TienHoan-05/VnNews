package com.example.vnnews.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.vnnews.NewsApplication
import com.example.vnnews.data.local.UserPreferencesRepository
import com.example.vnnews.data.remote.RssItemDto
import com.example.vnnews.data.repository.NewsCategory
import com.example.vnnews.data.repository.NewsRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.io.IOException

sealed interface HomeUiState {
    data object Loading : HomeUiState
    data class Success(
        val articles: List<RssItemDto>,
        val categories: List<NewsCategory>,
        val selectedCategory: NewsCategory,
        val bookmarkedUrls: Set<String> = emptySet(),
        val unavailableSources: List<String> = emptyList()
    ) : HomeUiState
    data class Error(val message: String) : HomeUiState
}

class HomeViewModel(
    private val newsRepository: NewsRepository,
    private val userPreferencesRepository: UserPreferencesRepository? = null
) : ViewModel() {

    private val _uiState = MutableStateFlow<HomeUiState>(HomeUiState.Loading)
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    private val categories = newsRepository.getCategories()
    private var currentCategory = categories.first()
    private val _selectedCategory = MutableStateFlow(currentCategory)
    val selectedCategory = _selectedCategory.asStateFlow()
    private var fetchJob: Job? = null
    val sources = newsRepository.getSources()
    private val _selectedSource = MutableStateFlow<String?>(null)
    val selectedSource = _selectedSource.asStateFlow()
    val categoriesList get() = categories

    fun selectSource(id: String?) {
        if (_selectedSource.value == id) return
        _selectedSource.value = id
        loadNews(currentCategory)
    }

    init {
        observeBookmarks()
        loadInitialNews()
    }

    private fun loadInitialNews() {
        viewModelScope.launch {
            val savedCategoryId = try {
                userPreferencesRepository?.selectedCategory?.first()
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                null
            }
            val initialCategory = categories.find { it.id == savedCategoryId } ?: categories.first()
            currentCategory = initialCategory
            _selectedCategory.value = initialCategory
            loadNews(initialCategory)
        }
    }

    private fun observeBookmarks() {
        viewModelScope.launch {
            newsRepository.getBookmarkedArticles().collect { bookmarkedList ->
                val urls = bookmarkedList.map { it.link }.toSet()
                val current = _uiState.value
                if (current is HomeUiState.Success) {
                    _uiState.value = current.copy(bookmarkedUrls = urls)
                }
            }
        }
    }

    fun selectCategory(category: NewsCategory) {
        if (currentCategory.id == category.id && _uiState.value is HomeUiState.Success) return
        currentCategory = category
        _selectedCategory.value = category
        viewModelScope.launch {
            try {
                userPreferencesRepository?.saveSelectedCategory(category.id)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                // Log or ignore to prevent UI disruption
            }
        }
        loadNews(category)
    }

    fun retry() {
        loadNews(currentCategory)
    }

    fun toggleBookmark(item: RssItemDto) {
        viewModelScope.launch {
            newsRepository.toggleBookmark(item)
        }
    }

    private fun loadNews(category: NewsCategory) {
        _uiState.value = HomeUiState.Loading
        fetchJob?.cancel()
        fetchJob = viewModelScope.launch {
            try {
                val selected = sources.filter { _selectedSource.value == null || it.id == _selectedSource.value }
                val failed = mutableListOf<String>()
                val articles = if (selected.isEmpty()) newsRepository.getNews(category.rssUrl) else coroutineScope {
                    selected.map { source -> async {
                        try {
                            val url = source.feeds[category.id] ?: error("Chưa có chuyên mục")
                            newsRepository.getNews(url).also { if (it.isEmpty()) error("Nguồn chưa có tin") }
                                .map { it.copy(sourceName = source.title) } to null
                        } catch (e: CancellationException) { throw e
                        } catch (e: Exception) { emptyList<RssItemDto>() to source.title }
                    } }.awaitAll().flatMap { (items, failure) ->
                        failure?.let { failed.add(it) }
                        items
                    }.sortedByDescending { it.publishedAt() ?: java.time.Instant.EPOCH }.distinctBy { it.link }
                }
                if (articles.isEmpty() && failed.isNotEmpty()) throw IOException("Không tải được nguồn báo")
                val bookmarkedUrls = try {
                    newsRepository.getBookmarkedArticles().first().map { it.link }.toSet()
                } catch (e: CancellationException) {
                    throw e
                } catch (e: Exception) {
                    emptySet()
                }
                _uiState.value = HomeUiState.Success(
                    articles = articles,
                    categories = categories,
                    selectedCategory = category,
                    bookmarkedUrls = bookmarkedUrls,
                    unavailableSources = failed
                )
            } catch (e: CancellationException) {
                throw e
            } catch (e: IOException) {
                _uiState.value = HomeUiState.Error("Không có kết nối mạng hoặc máy chủ không phản hồi. Vui lòng thử lại!")
            } catch (e: Exception) {
                _uiState.value = HomeUiState.Error("Đã xảy ra lỗi: ${e.localizedMessage ?: "Không xác định"}")
            }
        }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = (this[APPLICATION_KEY] as NewsApplication)
                HomeViewModel(
                    newsRepository = application.container.newsRepository,
                    userPreferencesRepository = application.container.userPreferencesRepository
                )
            }
        }
    }
}
