package io.getstream.chat.android.compose.ui.imagepreview

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.devbrackets.android.exomedia.listener.OnPreparedListener
import com.devbrackets.android.exomedia.ui.widget.VideoView
import io.getstream.chat.android.compose.R

/**
 * An Activity that is capable of playing video/audio stream.
 */
public class MediaPreviewActivity : AppCompatActivity(), OnPreparedListener {

    private lateinit var videoView: VideoView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.stream_compose_activity_media_preview)

        val url = intent.getStringExtra(KEY_URL)
        if (url.isNullOrEmpty()) {
            finish()
            return
        }
        setupVideoView(url)
    }

    override fun onPrepared() {
        videoView.start()
    }

    /**
     * Initializes the player with a media file to play.
     *
     * @param url The URL of the file to play.
     */
    private fun setupVideoView(url: String) {
        videoView = findViewById(R.id.videoView)
        videoView.isPlaying // workaround to init some internals of the library
        videoView.setVideoURI(Uri.parse(url))
        videoView.setOnPreparedListener(this)
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
