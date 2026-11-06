package com.example.vnnews

import com.example.vnnews.data.remote.NewsDates
import com.example.vnnews.data.remote.RssItemDto
import com.example.vnnews.data.repository.NewsRepository
import com.example.vnnews.data.repository.NewsSources
import com.example.vnnews.fake.FakeNewsRepository
import com.example.vnnews.ui.home.HomeUiState
import com.example.vnnews.ui.home.HomeViewModel
import com.example.vnnews.util.MainDispatcherRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import java.io.IOException
import java.time.Instant

@OptIn(ExperimentalCoroutinesApi::class)
class MultiSourceNewsTest {
    @get:Rule val dispatcher = MainDispatcherRule()

    private fun repository(fetch: suspend (String) -> List<RssItemDto>): NewsRepository =
        object : NewsRepository by FakeNewsRepository() {
            override fun getSources() = NewsSources.all
            override suspend fun getNews(categoryRssUrl: String) = fetch(categoryRssUrl)
        }

    @Test fun publicationDates_supportEachPublishersFormat() {
        val expected = Instant.parse("2026-11-06T08:00:00Z")
        listOf("Fri, 06 Nov 2026 15:00:00 +0700", "Fri, 06 Nov 26 15:00:00 +0700",
            "11/6/2026 3:00:00\u202fPM").forEach { assertEquals(it, expected, NewsDates.parse(it)) }
        assertNull(NewsDates.parse(null))
        assertNull(NewsDates.parse("not a date"))
    }

    @Test fun allSources_deduplicateAndOrderArticlesByPublicationTime() = runTest {
        val vm = HomeViewModel(repository { url ->
            if (url.contains("vnexpress")) listOf(
                RssItemDto("Earlier", "Fri, 06 Nov 2026 12:00:00 +0700", "https://example.com/earlier"),
                RssItemDto("Duplicate", "Fri, 06 Nov 2026 13:00:00 +0700", "https://example.com/same"))
            else listOf(RssItemDto("Later", "Fri, 06 Nov 26 15:00:00 +0700", "https://example.com/same"))
        })
        advanceUntilIdle()
        val state = vm.uiState.value as HomeUiState.Success
        assertEquals(2, state.articles.size)
        assertEquals("https://example.com/same", state.articles.first().link)
        assertTrue(state.articles.all { !it.sourceName.isNullOrBlank() })
    }

    @Test fun oneFailedSource_keepsOtherArticlesAndNamesUnavailablePublisher() = runTest {
        val vm = HomeViewModel(repository { url ->
            if (url.contains("tuoitre")) throw IOException("Unavailable")
            listOf(RssItemDto("News", link = url))
        })
        advanceUntilIdle()
        val state = vm.uiState.value as HomeUiState.Success
        assertEquals(3, state.articles.size)
        assertEquals(listOf("Tuổi Trẻ"), state.unavailableSources)
    }

    @Test fun allFailedSources_emitRetryableError() = runTest {
        val vm = HomeViewModel(repository { throw IOException("Offline") })
        advanceUntilIdle()
        assertTrue(vm.uiState.value is HomeUiState.Error)
    }

    @Test fun switchingSource_cancelsCombinedFetchAndShowsOnlySelectedPublisher() = runTest {
        val vm = HomeViewModel(repository { url -> delay(100); listOf(RssItemDto("News", link = url)) })
        vm.selectSource("dantri")
        advanceUntilIdle()
        val state = vm.uiState.value as HomeUiState.Success
        assertEquals(1, state.articles.size)
        assertEquals("Dân trí", state.articles.single().publisher())
        assertEquals("dantri", vm.selectedSource.value)
    }
}
