package com.example.vnnews.data.remote

import com.google.gson.annotations.SerializedName

data class RssFeedResponse(
    @SerializedName("status") val status: String,
    @SerializedName("items") val items: List<RssItemDto> = emptyList()
)

data class RssItemDto(
    @SerializedName("title") val title: String,
    @SerializedName("pubDate") val pubDate: String? = null,
    @SerializedName("link") val link: String,
    @SerializedName("guid") val guid: String? = null,
    @SerializedName("thumbnail") val thumbnail: String? = null,
    @SerializedName("description") val description: String? = null,
    @SerializedName("enclosure") val enclosure: EnclosureDto? = null,
    val sourceName: String? = null
) {
    val displayTitle: String by lazy(LazyThreadSafetyMode.PUBLICATION) { NewsText.title(title) }
    private val cachedDescription: String by lazy(LazyThreadSafetyMode.PUBLICATION) { NewsText.description(description) }
    private val publicationTime by lazy(LazyThreadSafetyMode.PUBLICATION) { NewsDates.parse(pubDate) }
    fun publisher(): String = sourceName ?: runCatching {
        val host = java.net.URI(link).host.orEmpty().removePrefix("www.")
        when (host) {
            "vnexpress.net" -> "VnExpress"
            "tuoitre.vn" -> "Tuổi Trẻ"
            "thanhnien.vn" -> "Thanh Niên"
            "dantri.com.vn" -> "Dân trí"
            else -> host
        }
    }.getOrDefault("")

    fun publishedAt(): java.time.Instant? = publicationTime
    fun getImageUrl(): String? {
        if (!thumbnail.isNullOrBlank()) return thumbnail
        if (!enclosure?.link.isNullOrBlank()) return enclosure?.link
        // Fallback: extract img src from description html if present
        description?.let { desc ->
            val regex = """src=["'](https?://[^"']+)["']""".toRegex()
            return regex.find(desc)?.groupValues?.get(1)
        }
        return null
    }

    fun getCleanDescription(): String {
        return cachedDescription
    }
}

data class EnclosureDto(
    @SerializedName("link") val link: String? = null
)
