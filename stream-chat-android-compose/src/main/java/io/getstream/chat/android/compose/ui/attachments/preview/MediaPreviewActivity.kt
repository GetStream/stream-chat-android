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

package io.getstream.chat.android.compose.ui.attachments.preview

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.Gravity
import android.view.KeyEvent
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.MediaController
import android.widget.ProgressBar
import android.widget.Toast
import android.widget.VideoView
import androidx.activity.SystemBarStyle
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.view.isVisible
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.compose.R
import io.getstream.chat.android.compose.ui.theme.ChatTheme
import io.getstream.chat.android.compose.ui.util.mirrorRtl

/**
 * An Activity that is capable of playing video/audio stream.
 */
public class MediaPreviewActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        setupEdgeToEdge()
        super.onCreate(savedInstanceState)
        val url = intent.getStringExtra(KEY_URL)
        val title = intent.getStringExtra(KEY_TITLE) ?: ""

        if (url.isNullOrEmpty() || ChatClient.isInitialized.not()) {
            finish()
            return
        }

        setContent {
            ChatTheme {
                MediaPreviewScreen(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black)
                        .windowInsetsPadding(WindowInsets.systemBars),
                    url = url,
                    title = title,
                    onPlaybackError = {
                        Toast.makeText(
                            this,
                            R.string.stream_ui_message_list_attachment_display_error,
                            Toast.LENGTH_SHORT,
                        ).show()
                        finish()
                    },
                    onBackPressed = { finish() },
                )
            }
        }
    }

    /**
     * Represents a screen with a media player.
     *
     * @param url The URL of the stream for playback.
     * @param title The name of the file for playback.
     * @param onPlaybackError Handler for playback errors.
     * @param onBackPressed Handler for back press action.
     */
    @Composable
    private fun MediaPreviewScreen(
        modifier: Modifier = Modifier,
        url: String,
        title: String,
        onPlaybackError: () -> Unit,
        onBackPressed: () -> Unit,
    ) {
        BackHandler(enabled = true, onBack = onBackPressed)

        Scaffold(
            modifier = modifier,
            backgroundColor = Color.Black,
            topBar = { MediaPreviewToolbar(title, onBackPressed) },
            content = { MediaPreviewContent(url, onBackPressed, onPlaybackError) },
        )
    }

    private fun setupEdgeToEdge() {
        val barsColor = Color.Black.toArgb()
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(barsColor),
            navigationBarStyle = SystemBarStyle.dark(barsColor),
        )
    }

    /**
     * Represents a toolbar with a back button and a file name.
     *
     * @param title The name of the file for playback.
     * @param onBackPressed Handler for back press action.
     */
    @Composable
    private fun MediaPreviewToolbar(
        title: String,
        onBackPressed: () -> Unit = {},
    ) {
        TopAppBar(
            backgroundColor = Color.Black,
            elevation = 0.dp,
            navigationIcon = {
                IconButton(
                    modifier = Modifier.mirrorRtl(LocalLayoutDirection.current),
                    onClick = { onBackPressed() },
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.stream_compose_ic_arrow_back),
                        contentDescription = null,
                        tint = Color.White,
                    )
                }
            },
            title = {
                Text(
                    text = title,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Start,
                    style = ChatTheme.typography.body,
                    maxLines = 1,
                    color = Color.White,
                )
            },
        )
    }

    /**
     * Represents a video player with media controls.
     *
     * @param url The URL of the stream for playback.
     * @param onBackPressed Handler for back press action.
     * @param onPlaybackError Handler for playback errors.
     */
    @Composable
    private fun MediaPreviewContent(
        url: String,
        onBackPressed: () -> Unit = {},
        onPlaybackError: () -> Unit,
    ) {
        val context = LocalContext.current

        val contentView = remember {
            val mediaController = createMediaController(context, onBackPressed)

            val frameLayout = FrameLayout(context).apply {
                layoutParams = FrameLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT,
                )
            }

            val progressBar = ProgressBar(context).apply {
                layoutParams = FrameLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                ).apply {
                    gravity = Gravity.CENTER
                }
            }

            progressBar.isVisible = true

            val videoView = VideoView(context).apply {
                setVideoURI(Uri.parse(url))
                setMediaController(mediaController)
                setOnErrorListener { _, _, _ ->
                    progressBar.isVisible = false
                    onPlaybackError()
                    true
                }
                setOnPreparedListener {
                    progressBar.isVisible = false
                    start()
                    mediaController.show()
                }
                mediaController.setAnchorView(frameLayout)

                layoutParams = FrameLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT,
                ).apply {
                    gravity = Gravity.CENTER
                }
            }

            frameLayout.apply {
                addView(videoView)
                addView(progressBar)
            }
        }

        AndroidView(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black),
            factory = { contentView },
        )
    }

    /**
     * Creates a custom instance of [MediaController] which no longer intercepts
     * back press actions to hide media controls.
     *
     * @param context The Context used to create the [MediaController].
     * @param onBackPressed Handler for back press action.
     */
    private fun createMediaController(
        context: Context,
        onBackPressed: () -> Unit = {},
    ): MediaController {
        return object : MediaController(context) {
            override fun dispatchKeyEvent(event: KeyEvent): Boolean {
                if (event.keyCode == KeyEvent.KEYCODE_BACK) {
                    onBackPressed()
                }
                return super.dispatchKeyEvent(event)
            }
        }
    }

    public companion object {
        private const val KEY_URL: String = "url"
        private const val KEY_TITLE: String = "title"

        /**
         * Used to build an [Intent] to start the [MediaPreviewActivity] with the required data.
         *
         * @param context The context to start the activity with.
         * @param url The URL of the media file.
         * @param title The name of the media file.
         */
        public fun getIntent(context: Context, url: String, title: String? = null): Intent {
            return Intent(context, MediaPreviewActivity::class.java).apply {
                putExtra(KEY_URL, url)
                putExtra(KEY_TITLE, title)
            }
        }
    }
}
