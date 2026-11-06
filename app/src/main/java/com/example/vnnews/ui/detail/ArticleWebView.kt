package com.example.vnnews.ui.detail

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.view.ViewGroup
import android.webkit.WebChromeClient
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun ArticleWebView(
    url: String,
    modifier: Modifier = Modifier,
    textZoom: Int = 100
) {
    var isLoading by remember { mutableStateOf(true) }
    var webView: WebView? by remember { mutableStateOf(null) }
    var canGoBack by remember { mutableStateOf(false) }
    var savedScrollY by rememberSaveable(url) { mutableStateOf(0) }

    BackHandler(enabled = canGoBack) {
        webView?.let {
            if (it.canGoBack()) {
                it.goBack()
                canGoBack = it.canGoBack()
            }
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { context ->
                val initialScrollY = savedScrollY
                var scrollRestored = false
                WebView(context).apply {
                    layoutParams = ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    )
                    settings.apply {
                        javaScriptEnabled = true
                        domStorageEnabled = true
                        useWideViewPort = true
                        loadWithOverviewMode = true
                        builtInZoomControls = true
                        displayZoomControls = false
                        this.textZoom = textZoom
                        mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
                        userAgentString = "Mozilla/5.0 (Linux; Android 14; Mobile) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Mobile Safari/537.36"
                    }
                    setOnScrollChangeListener { _, _, y, _, _ -> if (scrollRestored) savedScrollY = y }

                    webViewClient = object : WebViewClient() {
                        override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                            isLoading = true
                            canGoBack = view?.canGoBack() == true
                        }

                        override fun onPageFinished(view: WebView?, url: String?) {
                            isLoading = false
                            canGoBack = view?.canGoBack() == true
                            if (!scrollRestored) {
                                scrollRestored = true
                                if (initialScrollY > 0) view?.let { browser -> browser.post { browser.scrollTo(0, initialScrollY) } }
                            }
                        }

                        override fun doUpdateVisitedHistory(view: WebView?, url: String?, isReload: Boolean) {
                            super.doUpdateVisitedHistory(view, url, isReload)
                            canGoBack = view?.canGoBack() == true
                        }
                    }

                    webChromeClient = WebChromeClient()
                    loadUrl(url)
                    webView = this
                }
            },
            onRelease = { view ->
                view.stopLoading()
                view.setOnScrollChangeListener(null)
                view.destroy()
                if (webView === view) webView = null
            },
            update = { currentWebView ->
                webView = currentWebView
                if (currentWebView.settings.textZoom != textZoom) {
                    currentWebView.settings.textZoom = textZoom
                }
            }
        )

        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.align(Alignment.Center)
            )
        }
    }
}
