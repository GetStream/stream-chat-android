/*
 * Copyright (c) 2014-2022 Stream.io Inc. All rights reserved.
 *
 * Licensed under the Stream License;
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    https://github.com/GetStream/stream-chat-android/blob/main/LICENSE
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.getstream.chat.android.ui.feature.gallery

import android.os.Bundle
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import androidx.core.view.updatePadding
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.models.AttachmentType
import io.getstream.chat.android.ui.R
import io.getstream.chat.android.ui.databinding.StreamUiActivityAttachmentBinding
import io.getstream.chat.android.ui.utils.extensions.streamThemeInflater
import io.getstream.chat.android.ui.utils.load
import io.getstream.log.taggedLogger

/**
 * An Activity showing attachments such as websites, youtube and giphy.
 */
public class AttachmentActivity : AppCompatActivity() {
    private lateinit var binding: StreamUiActivityAttachmentBinding

    private val logger by taggedLogger("Chat:AttachmentActivity")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (ChatClient.isInitialized.not()) {
            finish()
            return
        }

        binding = StreamUiActivityAttachmentBinding.inflate(streamThemeInflater)
        setContentView(binding.root)

        setupEdgeToEdge()
        configUIs()

        val type = intent.getStringExtra("type")
        val url = intent.getStringExtra("url")
        if (type.isNullOrEmpty() || url.isNullOrEmpty()) {
            logger.e { "This file can't be displayed. TYPE or URL is missing." }
            Toast.makeText(this, getString(R.string.stream_ui_message_list_attachment_display_error), Toast.LENGTH_SHORT).show()
            return
        }
        showAttachment(type, url)
    }

    private fun configUIs() {
        binding.ivImage.isVisible = false
        binding.webView.isVisible = false

        // WebView
        binding.webView.apply {
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

    private fun setupEdgeToEdge() {
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { root, windowInsets ->
            val insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars())
            root.updatePadding(top = insets.top, left = insets.left, right = insets.right, bottom = insets.bottom)
            WindowInsetsCompat.CONSUMED
        }
    }

    private fun showAttachment(type: String, url: String) {
        when (type) {
            AttachmentType.GIPHY -> showGiphy(url)
            else -> loadUrlToWeb(url)
        }
    }

    /**
     * Show web view with url
     *
     * @param url web url
     */
    private fun loadUrlToWeb(url: String?) {
        binding.ivImage.isVisible = false
        binding.webView.isVisible = true
        binding.progressBar.isVisible = true

        url?.let {
            binding.webView.loadUrl(it)
        }
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
        binding.ivImage.isVisible = true
        binding.webView.isVisible = false

        binding.ivImage.load(
            data = url,
            placeholderResId = R.drawable.stream_ui_placeholder,
        )
    }

    private inner class AppWebViewClients : WebViewClient() {
        override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {
            view.loadUrl(url)
            return true
        }

        override fun onPageFinished(view: WebView, url: String) {
            super.onPageFinished(view, url)
            binding.progressBar.isVisible = false
        }

        override fun onReceivedError(view: WebView, request: WebResourceRequest, error: WebResourceError?) {
            logger.e { "The load failed due to an unknown error: $error" }
            if (error == null) {
                return
            }
            Toast.makeText(this@AttachmentActivity, error.toString(), Toast.LENGTH_SHORT).show()
        }
    }
}
