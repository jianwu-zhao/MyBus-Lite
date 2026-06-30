package com.mygolbs.mybus

import android.os.Bundle
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.appcompat.app.AppCompatActivity

/**
 * 掌上公交查询 - 加载掌上公交官方页面
 *
 * 直接用掌上公交真实的 H5 页面
 * 0591.mygolbs.com 是福州区站点
 * www.mygolbs.com 是官网
 */
class MainActivity : AppCompatActivity() {

    private lateinit var webView: WebView

    companion object {
        // 默认加载掌上公交官网（福州站）
        private const val DEFAULT_URL = "http://www.mygolbs.com"
        // 备用页面（如果官网打不开）
        private const val FALLBACK_URL = "http://0591.mygolbs.com/FuZhou/"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        webView = WebView(this).apply {
            settings.javaScriptEnabled = true
            settings.domStorageEnabled = true
            settings.useWideViewPort = true
            settings.loadWithOverviewMode = true
            settings.setSupportZoom(true)
            settings.builtInZoomControls = true
            settings.displayZoomControls = false
            settings.cacheMode = android.webkit.WebSettings.LOAD_DEFAULT

            webViewClient = object : WebViewClient() {
                override fun onReceivedError(view: WebView?, errorCode: Int, description: String?, failingUrl: String?) {
                    // 官网失败，加载备用
                    if (failingUrl == DEFAULT_URL || failingUrl == null) {
                        view?.loadUrl(FALLBACK_URL)
                    }
                }
            }
            webChromeClient = WebChromeClient()

            loadUrl(DEFAULT_URL)
        }

        setContentView(webView)
    }

    override fun onBackPressed() {
        if (webView.canGoBack()) {
            webView.goBack()
        } else {
            super.onBackPressed()
        }
    }
}