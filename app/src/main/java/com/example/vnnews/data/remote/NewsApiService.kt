package com.example.vnnews.data.remote

import okhttp3.ResponseBody
import retrofit2.http.GET
import retrofit2.http.Url
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

interface NewsApiService {
    suspend fun getNewsFeed(rssUrl: String): RssFeedResponse
}

interface VnExpressRawApi {
    @GET
    suspend fun getRawXml(@Url url: String): ResponseBody
}

class DefaultNewsApiService(
    private val rawApi: VnExpressRawApi
) : NewsApiService {
    override suspend fun getNewsFeed(rssUrl: String): RssFeedResponse {
        val items = withContext(Dispatchers.IO) {
            rawApi.getRawXml(rssUrl).use { body ->
                body.byteStream().use { VnExpressRssParser.parse(it) }
            }
        }
        return RssFeedResponse(
            status = "ok",
            items = items
        )
    }
}
