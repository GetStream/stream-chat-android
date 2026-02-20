/*
 * Copyright (c) 2014-2026 Stream.io Inc. All rights reserved.
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

package io.getstream.chat.android.ui.common.feature.documents

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.google.android.material.floatingactionbutton.FloatingActionButton
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.models.Attachment
import io.getstream.chat.android.ui.common.R
import io.getstream.chat.android.ui.common.internal.file.StreamShareFileManager
import io.getstream.chat.android.ui.common.utils.shareLocalFile
import io.getstream.log.StreamLog
import io.getstream.result.Result
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

/**
 * An Activity showing attachments such as PDF and Office documents.
 */
public open class AttachmentDocumentActivity : AppCompatActivity() {

    private lateinit var rootView: View
    private lateinit var webView: WebView
    private lateinit var progressBar: ProgressBar
    private lateinit var shareButton: FloatingActionButton

    private var reloadCount = 0
    private val maxReloadCount = 5

    private val shareFileManager = StreamShareFileManager()
    private var shareJob: Job? = null

    private val logger = StreamLog.getLogger("Chat:AttachmentDocumentActivity")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (!ChatClient.isInitialized) {
            finish()
            return
        }

        setContentView(R.layout.stream_activity_attachment_document)
        rootView = findViewById(R.id.rootView)
        webView = findViewById(R.id.webView)
        progressBar = findViewById(R.id.progressBar)
        shareButton = findViewById(R.id.shareButton)
        setupEdgeToEdge()
        configUIs()
        setupShareButton()

        val filePath = intent.getStringExtra(KEY_URL)
        loadDocument(filePath)
    }

    private fun setupEdgeToEdge() {
        ViewCompat.setOnApplyWindowInsetsListener(rootView) { v, insets ->
            val systemBarInsets = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBarInsets.left, systemBarInsets.top, systemBarInsets.right, systemBarInsets.bottom)
            WindowInsetsCompat.CONSUMED
        }
    }

    @Suppress("SetJavaScriptEnabled")
    private fun configUIs() {
        webView.settings.apply {
            javaScriptEnabled = true
            loadWithOverviewMode = true
            useWideViewPort = true
            builtInZoomControls = true
            @Suppress("DEPRECATION")
            pluginState = WebSettings.PluginState.ON
        }
        webView.webViewClient = DocumentWebViewClient()
    }

    private fun setupShareButton() {
        val url = intent.getStringExtra(KEY_URL)
        val mimeType = intent.getStringExtra(KEY_MIME_TYPE)

        if (url == null || mimeType == null) {
            shareButton.visibility = View.GONE
            return
        }

        val name = intent.getStringExtra(KEY_NAME)
        val fileSize = intent.getIntExtra(KEY_FILE_SIZE, 0)

        shareButton.visibility = View.VISIBLE
        shareButton.setOnClickListener { onShareClicked(url, mimeType, name, fileSize) }
    }

    private fun onShareClicked(url: String, mimeType: String, name: String?, fileSize: Int) {
        if (shareJob?.isActive == true) {
            shareJob?.cancel()
            shareJob = null
            shareButton.setImageResource(R.drawable.stream_ui_ic_share)
            progressBar.visibility = View.GONE
            return
        }

        progressBar.visibility = View.VISIBLE
        shareButton.setImageResource(android.R.drawable.ic_menu_close_clear_cancel)

        val attachment = Attachment(
            assetUrl = url,
            mimeType = mimeType,
            name = name,
            fileSize = fileSize,
        )
        shareJob = lifecycleScope.launch {
            val result = shareFileManager.writeAttachmentToShareableFile(this@AttachmentDocumentActivity, attachment)
            progressBar.visibility = View.GONE
            shareButton.setImageResource(R.drawable.stream_ui_ic_share)
            when (result) {
                is Result.Success -> shareLocalFile(result.value, mimeType)
                is Result.Failure -> Toast.makeText(
                    this@AttachmentDocumentActivity,
                    R.string.stream_ui_attachment_document_could_not_share,
                    Toast.LENGTH_SHORT,
                ).show()
            }
        }
    }

    private fun encodeUrl(url: String): String = try {
        URLEncoder.encode(url, StandardCharsets.UTF_8.name())
    } catch (@Suppress("SwallowedException") e: Exception) {
        url
    }

    /**
     * Load document as url.
     *
     * @param url Document url.
     */
    public fun loadDocument(url: String?) {
        progressBar.visibility = View.VISIBLE
        if (ChatClient.instance().isSocketConnected()) {
            webView.loadUrl("https://docs.google.com/gview?embedded=true&url=${encodeUrl(url ?: "")}")
        } else {
            finish()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        shareJob?.cancel()
    }

    private inner class DocumentWebViewClient : WebViewClient() {
        @Deprecated("Deprecated in Java")
        override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {
            view.loadUrl(url)
            return true
        }

        override fun onPageFinished(view: WebView, url: String) {
            if (view.title.isNullOrEmpty()) {
                if (reloadCount < maxReloadCount) {
                    view.reload()
                    reloadCount++
                } else {
                    progressBar.visibility = View.GONE
                    Toast.makeText(
                        this@AttachmentDocumentActivity,
                        R.string.stream_ui_message_list_attachment_load_failed,
                        Toast.LENGTH_SHORT,
                    ).show()
                }
            } else {
                progressBar.visibility = View.GONE
            }
        }

        override fun onReceivedError(view: WebView, request: WebResourceRequest, error: WebResourceError?) {
            logger.e { "The load failed due to an unknown error: $error" }
            if (error == null) return
            Toast.makeText(this@AttachmentDocumentActivity, error.toString(), Toast.LENGTH_SHORT).show()
        }
    }

    public companion object {
        private const val KEY_URL = "url"
        private const val KEY_MIME_TYPE = "mime_type"
        private const val KEY_NAME = "name"
        private const val KEY_FILE_SIZE = "file_size"

        @JvmStatic
        public fun getIntent(context: Context, url: String?): Intent =
            Intent(context, AttachmentDocumentActivity::class.java).apply {
                putExtra(KEY_URL, url)
            }

        /**
         * Creates an intent to open the document preview with full attachment metadata,
         * enabling the share button.
         *
         * @param context The context.
         * @param url The URL of the document.
         * @param mimeType The MIME type of the document.
         * @param name The file name of the document.
         * @param fileSize The file size in bytes.
         * @return An intent to start this activity.
         */
        @JvmStatic
        public fun getIntent(
            context: Context,
            url: String?,
            mimeType: String?,
            name: String?,
            fileSize: Int,
        ): Intent = Intent(context, AttachmentDocumentActivity::class.java).apply {
            putExtra(KEY_URL, url)
            if (mimeType != null) putExtra(KEY_MIME_TYPE, mimeType)
            if (name != null) putExtra(KEY_NAME, name)
            putExtra(KEY_FILE_SIZE, fileSize)
        }
    }
}
