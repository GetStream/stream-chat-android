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
import android.webkit.WebView
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.models.Attachment
import io.getstream.chat.android.ui.common.R
import io.getstream.chat.android.ui.common.internal.file.StreamShareFileManager
import io.getstream.chat.android.uiutils.model.MimeType
import io.getstream.log.taggedLogger
import io.getstream.result.onSuccessSuspend
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Activity for previewing text-based file attachments (TXT, HTML) in a WebView.
 *
 * The file is downloaded using [StreamShareFileManager] (which applies CDN transformations
 * via [ChatClient.downloadFile]), cached locally, and then rendered in a WebView.
 */
internal class TextFilePreviewActivity : AppCompatActivity() {

    private val logger by taggedLogger("Chat:TextFilePreviewActivity")
    private val shareFileManager = StreamShareFileManager()

    private lateinit var webView: WebView
    private lateinit var progressBar: ProgressBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (!ChatClient.isInitialized) {
            finish()
            return
        }

        setContentView(R.layout.stream_activity_text_file_preview)

        val rootView = findViewById<View>(R.id.rootView)
        webView = findViewById(R.id.webView)
        progressBar = findViewById(R.id.progressBar)

        setupEdgeToEdge(rootView)

        val url = intent.getStringExtra(KEY_URL)
        val mimeType = intent.getStringExtra(KEY_MIME_TYPE)
        val fileName = intent.getStringExtra(KEY_FILE_NAME)

        if (url.isNullOrEmpty()) {
            logger.e { "[onCreate] URL is null or empty. Finishing activity." }
            finish()
            return
        }

        loadFile(url, mimeType, fileName)
    }

    private fun setupEdgeToEdge(rootView: View) {
        ViewCompat.setOnApplyWindowInsetsListener(rootView) { v, insets ->
            val systemBarInsets = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBarInsets.left, systemBarInsets.top, systemBarInsets.right, systemBarInsets.bottom)
            WindowInsetsCompat.CONSUMED
        }
    }

    @Suppress("TooGenericExceptionCaught")
    private fun loadFile(url: String, mimeType: String?, fileName: String?) {
        progressBar.visibility = View.VISIBLE
        webView.visibility = View.GONE

        val attachment = Attachment(
            assetUrl = url,
            mimeType = mimeType,
            name = fileName ?: "file",
        )

        lifecycleScope.launch {
            val result = shareFileManager.writeAttachmentToShareableFile(this@TextFilePreviewActivity, attachment)
            result
                .onSuccessSuspend { uri ->
                    try {
                        val content = withContext(Dispatchers.IO) {
                            contentResolver.openInputStream(uri)?.bufferedReader()?.use { it.readText() }
                        }
                        if (content == null) {
                            showErrorAndFinish(fileName)
                            return@onSuccessSuspend
                        }
                        displayContent(content, mimeType)
                    } catch (e: Exception) {
                        logger.e(e) { "[loadFile] Failed to read cached file content." }
                        showErrorAndFinish(fileName)
                    }
                }
                .onError {
                    logger.e { "[loadFile] Failed to download file: ${it.message}" }
                    showErrorAndFinish(fileName)
                }
        }
    }

    private fun displayContent(content: String, mimeType: String?) {
        progressBar.visibility = View.GONE
        webView.visibility = View.VISIBLE

        val isHtml = mimeType == MimeType.MIME_TYPE_HTML
        if (isHtml) {
            webView.loadDataWithBaseURL(null, content, "text/html", "utf-8", null)
        } else {
            val wrappedContent = "<html><body><pre style=\"word-wrap: break-word; white-space: pre-wrap;\">" +
                "$content</pre></body></html>"
            webView.loadDataWithBaseURL(null, wrappedContent, "text/html", "utf-8", null)
        }
    }

    private fun showErrorAndFinish(fileName: String?) {
        val text = getString(
            R.string.stream_ui_message_list_attachment_download_failed,
            fileName ?: "",
        )
        Toast.makeText(this, text, Toast.LENGTH_SHORT).show()
        finish()
    }

    internal companion object {
        private const val KEY_URL = "url"
        private const val KEY_MIME_TYPE = "mimeType"
        private const val KEY_FILE_NAME = "fileName"

        /**
         * Creates an [Intent] to launch [TextFilePreviewActivity].
         *
         * @param context The context to create the intent from.
         * @param url The URL of the file to preview.
         * @param mimeType The MIME type of the file.
         * @param fileName The name of the file.
         */
        fun getIntent(context: Context, url: String, mimeType: String?, fileName: String?): Intent {
            return Intent(context, TextFilePreviewActivity::class.java).apply {
                putExtra(KEY_URL, url)
                putExtra(KEY_MIME_TYPE, mimeType)
                putExtra(KEY_FILE_NAME, fileName)
            }
        }
    }
}
