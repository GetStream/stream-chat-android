package io.getstream.chat.android.compose.ui.mediapreview

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.KeyEvent
import android.widget.MediaController
import android.widget.Toast
import android.widget.VideoView
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import io.getstream.chat.android.compose.R
import io.getstream.chat.android.compose.handlers.SystemBackPressedHandler
import io.getstream.chat.android.compose.ui.theme.ChatTheme
import io.getstream.chat.android.compose.ui.util.mirrorRtl

/**
 * An Activity that is capable of playing video/audio stream.
 */
public class MediaPreviewActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val url = intent.getStringExtra(KEY_URL)
        val title = intent.getStringExtra(KEY_TITLE) ?: ""

        if (url.isNullOrEmpty()) {
            finish()
            return
        }

        setContent {
            ChatTheme {
                SetupSystemUI()
                MediaPreviewScreen(
                    url = url,
                    title = title,
                    onPlaybackError = {
                        Toast.makeText(
                            this,
                            R.string.stream_ui_message_list_attachment_display_error,
                            Toast.LENGTH_SHORT
                        ).show()
                        finish()
                    },
                    onBackPressed = { finish() }
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
        url: String,
        title: String,
        onPlaybackError: () -> Unit,
        onBackPressed: () -> Unit = {},
    ) {
        val backgroundColor = Color.Black
        val controlsColor = Color.White

        SystemBackPressedHandler(isEnabled = true, onBackPressed = onBackPressed)

        Scaffold(
            modifier = Modifier.fillMaxSize(),
            backgroundColor = backgroundColor,
            topBar = {
                TopAppBar(
                    backgroundColor = backgroundColor,
                    elevation = 0.dp,
                    navigationIcon = {
                        val layoutDirection = LocalLayoutDirection.current

                        IconButton(
                            modifier = Modifier
                                .padding(8.dp)
                                .size(16.dp)
                                .mirrorRtl(layoutDirection = layoutDirection),
                            onClick = { finish() }
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.stream_compose_ic_arrow_back),
                                contentDescription = null,
                                tint = controlsColor,
                            )
                        }
                    },
                    title = {
                        Text(
                            text = title,
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.Start,
                            style = ChatTheme.typography.bodyBold,
                            maxLines = 1,
                            color = controlsColor
                        )
                    }
                )
            },
            content = {
                val context = LocalContext.current

                val videoView = remember {
                    val mediaController = createMediaController(context, onBackPressed)

                    VideoView(context).apply {
                        setVideoURI(Uri.parse(url))
                        setMediaController(mediaController)
                        setOnErrorListener { _, _, _ ->
                            onPlaybackError()
                            true
                        }
                        setOnPreparedListener {
                            start()
                            mediaController.show()
                        }
                    }
                }

                AndroidView(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(vertical = 16.dp)
                        .background(backgroundColor),
                    factory = { videoView },
                )
            }
        )
    }

    /**
     * Responsible for updating the system UI.
     */
    @Composable
    private fun SetupSystemUI() {
        val systemUiController = rememberSystemUiController()

        val statusBarColor = Color.Black
        val navigationBarColor = Color.Black

        SideEffect {
            systemUiController.setStatusBarColor(
                color = statusBarColor,
                darkIcons = false
            )
            systemUiController.setNavigationBarColor(
                color = navigationBarColor,
                darkIcons = false
            )
        }
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
