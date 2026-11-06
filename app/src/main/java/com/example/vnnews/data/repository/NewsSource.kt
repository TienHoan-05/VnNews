package com.example.vnnews.data.repository

data class NewsSource(val id: String, val title: String, val feeds: Map<String, String>)

object NewsSources {
    private val categories = listOf("tin-moi-nhat", "so-hoa", "kinh-doanh", "the-thao", "giai-tri")
    private fun source(id: String, title: String, base: String, paths: List<String>) =
        NewsSource(id, title, categories.zip(paths.map { "$base$it.rss" }).toMap())

    val all = listOf(
        source("vnexpress", "VnExpress", "https://vnexpress.net/rss/", listOf("tin-noi-bat", "khoa-hoc-cong-nghe", "kinh-doanh", "the-thao", "giai-tri")),
        source("tuoitre", "Tuổi Trẻ", "https://tuoitre.vn/rss/", listOf("tin-moi-nhat", "nhip-song-so", "kinh-doanh", "the-thao", "giai-tri")),
        source("thanhnien", "Thanh Niên", "https://thanhnien.vn/rss/", listOf("home", "cong-nghe", "kinh-te", "the-thao", "giai-tri")),
        source("dantri", "Dân trí", "https://dantri.com.vn/rss/", listOf("home", "cong-nghe", "kinh-doanh", "the-thao", "giai-tri"))
    )
}
