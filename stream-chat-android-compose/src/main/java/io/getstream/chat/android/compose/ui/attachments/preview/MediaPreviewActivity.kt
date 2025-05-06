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
import android.os.Bundle
import android.widget.Toast
import androidx.activity.SystemBarStyle
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.compose.R
import io.getstream.chat.android.compose.ui.attachments.preview.internal.StreamVideoPlayerContent
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
            containerColor = Color.Black,
            topBar = { MediaPreviewToolbar(title, onBackPressed) },
            content = { padding ->
                StreamVideoPlayerContent(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    assetUrl = url,
                    playWhenReady = true,
                    showControllerInitially = true,
                    thumbnailEnabled = false,
                    onPlaybackError = onPlaybackError,
                )
            },
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
    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    private fun MediaPreviewToolbar(
        title: String,
        onBackPressed: () -> Unit = {},
    ) {
        TopAppBar(
            colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Black),
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
