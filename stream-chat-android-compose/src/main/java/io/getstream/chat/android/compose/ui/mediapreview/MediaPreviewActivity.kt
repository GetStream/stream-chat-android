package io.getstream.chat.android.compose.ui.mediapreview

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.runtime.Composable
import androidx.compose.ui.viewinterop.AndroidView
import com.devbrackets.android.exomedia.listener.OnPreparedListener
import com.devbrackets.android.exomedia.ui.widget.VideoView
import com.devbrackets.android.exomedia.ui.widget.controls.VideoControlsMobile
import io.getstream.chat.android.compose.ui.theme.ChatTheme

/**
 * An Activity that is capable of playing video/audio stream.
 */
public class MediaPreviewActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val url = intent.getStringExtra(KEY_URL)

        if (url.isNullOrEmpty()) {
            finish()
            return
        }

        setContent {
            ChatTheme {
                MediaPreviewContentWrapper(url)
            }
        }
    }

    @Composable
    private fun MediaPreviewContentWrapper(url: String) {
        val videoView = VideoView(this)
        videoView.isPlaying // Workaround to init some internals of the library
        videoView.videoControls = VideoControlsMobile(this)
        videoView.setOnPreparedListener(
            object : OnPreparedListener {
                override fun onPrepared() {
                    videoView.start()
                }
            }
        )
        videoView.setVideoURI(Uri.parse(url))

        AndroidView(
            factory = {
                videoView
            }
        )
    }

    public companion object {
        private const val KEY_URL: String = "url"

        /**
         * Used to build an [Intent] to start the [MediaPreviewActivity] with the required data.
         *
         * @param context The context to start the activity with.
         * @param url The URL of the file.
         */
        public fun getIntent(context: Context, url: String): Intent {
            return Intent(context, MediaPreviewActivity::class.java).apply {
                putExtra(KEY_URL, url)
            }
        }
    }
}
