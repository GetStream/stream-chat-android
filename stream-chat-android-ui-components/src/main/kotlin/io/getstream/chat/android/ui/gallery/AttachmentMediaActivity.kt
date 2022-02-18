package io.getstream.chat.android.ui.gallery

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.KeyEvent
import android.widget.MediaController
import android.widget.Toast
import android.widget.VideoView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import io.getstream.chat.android.client.logger.ChatLogger
import io.getstream.chat.android.ui.R
import io.getstream.chat.android.ui.common.extensions.internal.getColorCompat
import io.getstream.chat.android.ui.common.extensions.internal.streamThemeInflater
import io.getstream.chat.android.ui.databinding.StreamUiActivityAttachmentMediaBinding

/**
 * An Activity playing attachments such as audio and video.
 */
public class AttachmentMediaActivity : AppCompatActivity() {
    private lateinit var binding: StreamUiActivityAttachmentMediaBinding

    private val url: String? by lazy { intent.getStringExtra(KEY_URL) }
    private val title: String? by lazy { intent.getStringExtra(KEY_TITLE) }
    private val type: String? by lazy { intent.getStringExtra(KEY_TYPE) }
    private val mimeType: String? by lazy { intent.getStringExtra(KEY_MIME_TYPE) }

    private val logger = ChatLogger.get("AttachmentMediaActivity")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = StreamUiActivityAttachmentMediaBinding.inflate(streamThemeInflater)
        setContentView(binding.root)

        if ((type.isNullOrEmpty() && mimeType.isNullOrEmpty()) || url.isNullOrEmpty()) {
            logger.logE("This file can't be displayed. The TYPE or the URL are null")
            Toast.makeText(
                this,
                R.string.stream_ui_message_list_attachment_display_error,
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        setupSystemUi()
        setupViews()
        setupVideoView()
    }

    /**
     * Initializes the toolbar and audio icon.
     */
    private fun setupViews() {
        binding.headerLeftActionButton.setOnClickListener { onBackPressed() }
        binding.headerTitleTextView.text = title
        binding.ivAudio.isVisible = type?.contains("audio") == true || mimeType?.contains("audio") == true
    }

    /**
     * Responsible for updating the system UI.
     */
    private fun setupSystemUi() {
        window.navigationBarColor = getColorCompat(R.color.stream_ui_literal_black)
        window.statusBarColor = getColorCompat(R.color.stream_ui_literal_black)
    }

    /**
     * Initializes the [VideoView] with media controls and starts the playback.
     */
    private fun setupVideoView() {
        val mediaController = createMediaController(this)
        mediaController.setAnchorView(binding.contentContainer)

        binding.videoView.apply {
            setMediaController(mediaController)
            setOnPreparedListener {
                start()
                mediaController.show()
            }
            setVideoURI(Uri.parse(url))
        }
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
         * @param context
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
