package com.getstream.sdk.chat.view.activity

import android.net.Uri
import android.os.Bundle
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.ImageView
import android.widget.MediaController
import android.widget.ProgressBar
import android.widget.Toast
import android.widget.VideoView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import com.getstream.sdk.chat.ChatUI
import com.getstream.sdk.chat.UrlSigner
import com.getstream.sdk.chat.images.load
import com.getstream.sdk.chat.model.ModelType
import io.getstream.chat.android.client.logger.ChatLogger
import io.getstream.chat.android.ui.common.R

/**
 * An Activity showing attachments such as websites, youtube and giphy.
 */
public class AttachmentActivity : AppCompatActivity() {
    private lateinit var webView: WebView
    private lateinit var iv_image: ImageView
    private lateinit var progressBar: ProgressBar
    private lateinit var videoView: VideoView

    private val logger = ChatLogger.get("AttachmentActivity")

    private val urlSigner: UrlSigner
        get() = ChatUI.instance().urlSigner

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.stream_activity_attachment)

        webView = findViewById(R.id.webView)
        iv_image = findViewById(R.id.iv_image)
        progressBar = findViewById(R.id.progressBar)
        videoView = findViewById(R.id.videoView)

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
        videoView.isVisible = false

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
        when (type) {
            ModelType.attach_giphy -> showGiphy(url)
            ModelType.attach_video -> showVideo(url)
            else -> loadUrlToWeb(url)
        }
    }

    private fun showVideo(url: String) {
        iv_image.isVisible = false
        webView.isVisible = false
        progressBar.isVisible = true
        videoView.isVisible = true

        try {
            val mc = MediaController(this).apply {
                setAnchorView(videoView)
                setMediaPlayer(videoView)
            }
            videoView.run {
                setMediaController(mc)
                setVideoURI(Uri.parse(url))
                setOnPreparedListener { progressBar.isVisible = false }
                requestFocus()
                start()
            }
        } catch (e: Exception) {
            logger.logE("Exception during playing video\n$e")
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
        videoView.isVisible = false

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
        videoView.isVisible = false

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
