package com.example.vnnews

import com.example.vnnews.data.remote.NewsText
import com.example.vnnews.data.remote.RssItemDto
import org.junit.Assert.*
import org.junit.Test

class NewsTextTest {
    @Test fun decodesVietnameseNumericEntitiesAndNormalizesCombiningMarks() {
        assertEquals("Tiếng Việt & đời sống", NewsText.title("Ti&#7871;ng Vi&#x1ec7;t &amp; đời sống"))
        assertEquals("Tiếng Việt", NewsText.title("Tie\u0302\u0301ng Vie\u0323\u0302t"))
    }
    @Test fun removesMarkupWithoutLeavingEncodedGarbageInDescriptions() {
        val dto = RssItemDto("Tin", link = "https://example.com", description =
            "<a href='x'><img src='x'/></a><p>H&#224;&nbsp;Nội &amp; TP.HCM</p><script>bad()</script><p>Tin mới</p>")
        assertEquals("Hà Nội & TP.HCM Tin mới", dto.getCleanDescription())
        repeat(1000) { assertEquals("Hà Nội & TP.HCM Tin mới", dto.getCleanDescription()) }
    }
    @Test fun keepsComparisonsAndEmojiAndRemovesInvisibleFeedControls() {
        assertEquals("Giá < 100 & > 50 🚀", NewsText.title("Giá &lt; 100 &amp; &gt; 50 🚀\u0000\u200b"))
    }
}
