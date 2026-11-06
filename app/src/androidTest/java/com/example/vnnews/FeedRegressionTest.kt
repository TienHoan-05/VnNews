package com.example.vnnews

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.StateRestorationTester
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.emptyPreferences
import androidx.lifecycle.ViewModelStore
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.vnnews.data.local.*
import com.example.vnnews.data.remote.RssItemDto
import com.example.vnnews.data.repository.*
import com.example.vnnews.ui.home.*
import com.example.vnnews.ui.theme.VnNewsTheme
import kotlinx.coroutines.flow.*
import org.junit.After
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import java.time.Instant

class FeedRegressionTest {
    @get:Rule val rule = createComposeRule()
    private val store = ViewModelStore()
    private lateinit var home: HomeViewModel
    private lateinit var weather: WeatherViewModel
    private lateinit var list: LazyListState

    private fun models() {
        rule.runOnUiThread {
            val preferences = object : DataStore<Preferences> {
                private val state = MutableStateFlow(emptyPreferences())
                override val data: Flow<Preferences> = state
                override suspend fun updateData(transform: suspend (Preferences) -> Preferences): Preferences {
                    val next = transform(state.value); state.value = next; return next
                }
            }
            val repository = object : NewsRepository {
                override fun getCategories() = listOf(NewsCategory("tin-moi-nhat", "Mới nhất", "https://example.com/feed"))
                override suspend fun getNews(categoryRssUrl: String) = (0..99).map {
                    RssItemDto("Tiếng Việt số $it · Đời sống ở Hà Nội", link = "https://example.com/$it")
                }
                override fun getBookmarkedArticles(): Flow<List<ArticleEntity>> = flowOf(emptyList())
                override fun isBookmarkedFlow(link: String) = flowOf(false)
                override suspend fun toggleBookmark(item: RssItemDto) {}
                override suspend fun removeBookmark(link: String) {}
            }
            home = HomeViewModel(repository)
            weather = WeatherViewModel(object : WeatherRepository {
                override suspend fun getWeather(city: WeatherCity) = LocalWeather(28.0, 0, Instant.now())
            }, UserPreferencesRepository(preferences))
            store.put("home", home); store.put("weather", weather)
        }
    }
    @After fun clearModels() { rule.runOnUiThread { store.clear() } }

    @Test fun articleRoundTrip_keepsExactScrollPosition() {
        models()
        lateinit var nav: NavHostController
        rule.setContent {
            VnNewsTheme {
                nav = rememberNavController()
                NavHost(navController = nav, startDestination = "feed") {
                    composable("feed") {
                        list = rememberLazyListState()
                        HomeScreen({ _, _ -> nav.navigate("article") }, Modifier.fillMaxSize(), home, weather, list)
                    }
                    composable("article") { Text("Bài báo") }
                }
            }
        }
        rule.onNodeWithTag("news-list").performScrollToIndex(35)
        var before = 0 to 0
        rule.runOnIdle { before = list.firstVisibleItemIndex to list.firstVisibleItemScrollOffset }
        rule.onNodeWithText("Tiếng Việt số 32 · Đời sống ở Hà Nội").performClick()
        rule.onNodeWithText("Bài báo").assertIsDisplayed()
        rule.runOnIdle { nav.popBackStack() }
        rule.waitForIdle()
        rule.runOnIdle { assertEquals(before, list.firstVisibleItemIndex to list.firstVisibleItemScrollOffset) }
    }

    @Test fun restoredScreen_keepsScrollInsteadOfRunningAnInitialScrollToTop() {
        models()
        val restoration = StateRestorationTester(rule)
        restoration.setContent {
            VnNewsTheme {
                list = rememberLazyListState()
                HomeScreen({ _, _ -> }, Modifier.fillMaxSize(), home, weather, list)
            }
        }
        rule.onNodeWithTag("news-list").performScrollToIndex(45)
        var before = 0 to 0
        rule.runOnIdle { before = list.firstVisibleItemIndex to list.firstVisibleItemScrollOffset }
        restoration.emulateSavedInstanceStateRestore()
        rule.runOnIdle { assertEquals(before, list.firstVisibleItemIndex to list.firstVisibleItemScrollOffset) }
    }

    @Test fun repeatedFastScrolling_doesNotMixVietnameseTitlesBetweenRecycledRows() {
        models()
        rule.setContent { VnNewsTheme { HomeScreen({ _, _ -> }, viewModel = home, weatherViewModel = weather) } }
        // Three header items precede the articles in compact portrait mode.
        listOf(80, 15, 65, 25, 90, 40).forEach { index ->
            rule.onNodeWithTag("news-list").performScrollToIndex(index + 3)
            rule.onNodeWithText("Tiếng Việt số $index · Đời sống ở Hà Nội").assertExists()
        }
    }
}
