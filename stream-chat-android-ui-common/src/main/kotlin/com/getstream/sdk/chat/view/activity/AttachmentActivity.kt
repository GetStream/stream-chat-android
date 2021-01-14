package com.getstream.sdk.chat.view.activity

import android.os.Bundle
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import com.getstream.sdk.chat.ChatUI
import com.getstream.sdk.chat.UrlSigner
import com.getstream.sdk.chat.images.load
import com.getstream.sdk.chat.model.ModelType
import io.getstream.chat.android.client.logger.ChatLogger.Companion.get
import io.getstream.chat.android.ui.common.R

/**
 * An Activity showing attachments such as websites, youtube and giphy.
 */
public class AttachmentActivity : AppCompatActivity() {
    private lateinit var webView: WebView
    private lateinit var iv_image: ImageView
    private lateinit var progressBar: ProgressBar

    private val logger = get("AttachmentActivity")

    private val urlSigner: UrlSigner
        get() = ChatUI.instance().urlSigner

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.stream_activity_attachment)

        webView = findViewById(R.id.webView)
        iv_image = findViewById(R.id.iv_image)
        progressBar = findViewById(R.id.progressBar)

        configUIs()

        val type = intent.getStringExtra("type")
        val url = intent.getStringExtra("url")
        if (type.isNullOrEmpty() || url.isNullOrEmpty()) {
            logger.logE("This file can't be displayed. TYPE or URL is missing.")
            Toast.makeText(this, getString(R.string.stream_ui_attachment_display_error), Toast.LENGTH_SHORT).show()
            return
        }
        showAttachment(type, url)
    }

    private fun configUIs() {
        iv_image.isVisible = false
        webView.isVisible = false

        // WebView
        webView.apply {
            settings.apply {
                javaScriptEnabled = true
                loadWithOverviewMode = true
                useWideViewPort = true
                builtInZoomControls = true
                pluginState = WebSettings.PluginState.ON
            }
            webViewClient = AppWebViewClients()
        }
    }

    private fun showAttachment(type: String, url: String) {
        if (type == ModelType.attach_giphy) {
            showGiphy(url)
        } else {
            loadUrlToWeb(url)
        }
    }

    /**
     * Show web view with url
     *
     * @param url web url
     */
    private fun loadUrlToWeb(url: String?) {
        iv_image.isVisible = false
        webView.isVisible = true
        progressBar.isVisible = true

        webView.loadUrl(urlSigner.signFileUrl(url))
    }

    /**
     * Play giphy with url
     *
     * @param url giphy url
     */
    private fun showGiphy(url: String?) {
        if (url == null) {
            Toast.makeText(this, "Error!", Toast.LENGTH_SHORT).show()
            return
        }
        iv_image.isVisible = true
        webView.isVisible = false

        iv_image.load(
            data = urlSigner.signImageUrl(url),
            placeholderResId = R.drawable.stream_placeholder
        )
    }

    private inner class AppWebViewClients : WebViewClient() {
        override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {
            view.loadUrl(urlSigner.signFileUrl(url))
            return true
        }

        override fun onPageFinished(view: WebView, url: String) {
            super.onPageFinished(view, urlSigner.signFileUrl(url))
            progressBar.isVisible = false
        }

        override fun onReceivedError(view: WebView, request: WebResourceRequest, error: WebResourceError?) {
            logger.logE("The load failed due to an unknown error: $error")
            if (error == null) {
                return
            }
            Toast.makeText(this@AttachmentActivity, error.toString(), Toast.LENGTH_SHORT).show()
        }
    }
}
