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

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.KeyEvent
import android.widget.MediaController
import android.widget.Toast
import android.widget.VideoView
import androidx.activity.SystemBarStyle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import androidx.core.view.updatePadding
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.ui.R
import io.getstream.chat.android.ui.databinding.StreamUiActivityAttachmentMediaBinding
import io.getstream.chat.android.ui.utils.extensions.getColorCompat
import io.getstream.chat.android.ui.utils.extensions.streamThemeInflater
import io.getstream.log.taggedLogger

/**
 * An Activity playing attachments such as audio and video.
 */
public class AttachmentMediaActivity : AppCompatActivity() {
    private lateinit var binding: StreamUiActivityAttachmentMediaBinding

    private val url: String? by lazy { intent.getStringExtra(KEY_URL) }
    private val title: String? by lazy { intent.getStringExtra(KEY_TITLE) }
    private val type: String? by lazy { intent.getStringExtra(KEY_TYPE) }
    private val mimeType: String? by lazy { intent.getStringExtra(KEY_MIME_TYPE) }

    private val logger by taggedLogger("Chat:AttachmentMediaActivity")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (ChatClient.isInitialized.not()) {
            finish()
            return
        }

        binding = StreamUiActivityAttachmentMediaBinding.inflate(streamThemeInflater)
        setContentView(binding.root)

        if ((type.isNullOrEmpty() && mimeType.isNullOrEmpty()) || url.isNullOrEmpty()) {
            logger.e { "This file can't be displayed. The TYPE or the URL are null" }
            showPlaybackError()
            return
        }

        setupEdgeToEdge()
        setupViews()
        setupVideoView()
    }

    /**
     * Initializes the toolbar and audio icon.
     */
    private fun setupViews() {
        binding.headerLeftActionButton.setOnClickListener { onBackPressed() }
        binding.headerTitleTextView.text = title
        binding.audioImageView.isVisible = type?.contains("audio") == true || mimeType?.contains("audio") == true
    }

    /**
     * Responsible for updating the system UI.
     */
    private fun setupEdgeToEdge() {
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(getColorCompat(R.color.stream_ui_literal_black)),
            navigationBarStyle = SystemBarStyle.dark(getColorCompat(R.color.stream_ui_literal_black)),
        )
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, windowInsets ->
            val insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.updatePadding(top = insets.top, left = insets.left, right = insets.right, bottom = insets.bottom)

            WindowInsetsCompat.CONSUMED
        }
    }

    /**
     * Initializes the [VideoView] with media controls and starts the playback.
     */
    private fun setupVideoView() {
        val mediaController = createMediaController(this)
        mediaController.setAnchorView(binding.contentContainer)

        binding.progressBar.isVisible = true
        binding.videoView.apply {
            setMediaController(mediaController)
            setOnPreparedListener {
                binding.progressBar.isVisible = false
                start()
                mediaController.show()
            }
            setOnErrorListener { _, _, _ ->
                binding.progressBar.isVisible = false
                showPlaybackError()
                true
            }
            setVideoURI(Uri.parse(url))
        }
    }

    /**
     * Displays a Toast with an error if there was an issue playing the video.
     */
    private fun showPlaybackError() {
        Toast.makeText(
            this,
            R.string.stream_ui_message_list_attachment_display_error,
            Toast.LENGTH_SHORT,
        ).show()
    }

    /**
     * Creates a custom instance of [MediaController] which no longer intercepts
     * back press actions to hide media controls.
     *
     * @param context The Context used to create the [MediaController].
     */
    private fun createMediaController(context: Context): MediaController {
        return object : MediaController(context) {
            override fun dispatchKeyEvent(event: KeyEvent): Boolean {
                if (event.keyCode == KeyEvent.KEYCODE_BACK) {
                    finish()
                }
                return super.dispatchKeyEvent(event)
            }
        }
    }

    public companion object {
        /**
         * Represents the key for the media URL.
         */
        private const val KEY_URL: String = "url"

        /**
         * Represents the key for the title that will be shown in the header.
         */
        private const val KEY_TITLE: String = "title"

        /**
         * Represents the key for the MIME type of the file.
         */
        private const val KEY_MIME_TYPE: String = "mime_type"

        /**
         * Represents the key for attachment type.
         */
        private const val KEY_TYPE: String = "type"

        /**
         * Used to build an [Intent] to start the [AttachmentMediaActivity] with the required data.
         *
         * @param context The context to start the activity with.
         * @param url The URL of the media file.
         * @param title The title that will be shown in the header.
         * @param mimeType The MIME type of the file.
         * @param type The type of the attachment.
         * @return The [Intent] to start the [AttachmentMediaActivity].
         */
        public fun createIntent(
            context: Context,
            url: String,
            title: String,
            mimeType: String? = null,
            type: String? = null,
        ): Intent {
            return Intent(context, AttachmentMediaActivity::class.java).apply {
                putExtra(KEY_URL, url)
                putExtra(KEY_TITLE, title)
                putExtra(KEY_MIME_TYPE, mimeType)
                putExtra(KEY_TYPE, type)
            }
        }
    }
}
