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

package io.getstream.chat.android.ui.feature.gallery

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.SystemBarStyle
import androidx.activity.enableEdgeToEdge
import androidx.annotation.OptIn
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import androidx.core.view.updatePadding
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
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

    private var player: Player? = null

    /**
     * Saved playback position for restoration after app resume.
     */
    private var savedPlaybackPosition: Long = 0L
    private var autoPlay: Boolean = true

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
    }

    override fun onStart() {
        super.onStart()
        // (Re)create player when returning to foreground.
        player = createPlayer()
            .apply {
                setMediaItem(MediaItem.fromUri(Uri.parse(url)), savedPlaybackPosition)
                prepare()
                playWhenReady = autoPlay
            }
            .also(::setupPlayerView)
    }

    override fun onPause() {
        super.onPause()
        player?.pause()
    }

    override fun onStop() {
        super.onStop()
        // Save playback position and release player to free wake lock.
        savedPlaybackPosition = player?.currentPosition ?: 0L
        autoPlay = false
        binding.playerView.player = null
        player?.release()
        player = null
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

    private fun createPlayer(): Player {
        val player = ExoPlayer.Builder(this).build()
        player.addListener(object : Player.Listener {
            override fun onPlaybackStateChanged(playbackState: Int) {
                val isBuffering = playbackState == Player.STATE_BUFFERING
                binding.progressBar.isVisible = isBuffering
            }

            override fun onPlayerError(error: PlaybackException) {
                showPlaybackError()
            }
        })
        return player
    }

    @OptIn(UnstableApi::class)
    private fun setupPlayerView(player: Player) {
        binding.playerView.apply {
            this.player = player
            setOnClickListener {
                if (binding.controls.isVisible) {
                    binding.controls.hide()
                } else {
                    binding.controls.show()
                }
            }
            setControllerHideOnTouch(true)
            setShowBuffering(PlayerView.SHOW_BUFFERING_NEVER)
            setArtworkDisplayMode(PlayerView.ARTWORK_DISPLAY_MODE_OFF)
            // Disable default controller because we use the legacy one
            setUseController(false)
        }

        // Setup legacy controller
        binding.controls.player = player
        binding.controls.showTimeoutMs = CONTROLLER_SHOW_TIMEOUT
        binding.controls.setShowNextButton(false)
        binding.controls.setShowPreviousButton(false)
        binding.controls.show()
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

        private const val CONTROLLER_SHOW_TIMEOUT = 2000

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
