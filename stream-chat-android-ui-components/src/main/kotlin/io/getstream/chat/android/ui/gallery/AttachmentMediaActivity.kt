package io.getstream.chat.android.ui.gallery

import android.os.Bundle
import io.getstream.chat.android.ui.R
import android.net.Uri
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import com.devbrackets.android.exomedia.listener.OnPreparedListener
import io.getstream.chat.android.client.logger.ChatLogger
import io.getstream.chat.android.ui.common.extensions.internal.streamThemeInflater
import io.getstream.chat.android.ui.databinding.StreamUiActivityAttachmentMediaBinding

/**
 * An Activity playing attachments such as audio and video.
 */
public class AttachmentMediaActivity : AppCompatActivity() {
    private lateinit var binding: StreamUiActivityAttachmentMediaBinding

    private val logger = ChatLogger.get("AttachmentMediaActivity")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = StreamUiActivityAttachmentMediaBinding.inflate(streamThemeInflater)
        setContentView(binding.root)
        init()
    }

    private fun init() {
        val type = intent.getStringExtra(TYPE_KEY)
        val url = intent.getStringExtra(URL_KEY)
        if (type.isNullOrEmpty() || url.isNullOrEmpty()) {
            logger.logE("This file can't be displayed. The TYPE or the URL are null")
            Toast.makeText(
                this,
                R.string.stream_ui_message_list_attachment_display_error,
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        binding.ivAudio.isVisible = "audio" in type

        playVideo(url)
    }

    /**
     * Play media file with url
     *
     * @param url media url
     */
    private fun playVideo(url: String?) {
        binding.videoView.isPlaying // Workaround to init some internals of the library
        binding.videoView.setVideoURI(Uri.parse(url))
        binding.videoView.setOnPreparedListener(object : OnPreparedListener {
            override fun onPrepared() {
                binding.videoView.start()
            }
        })
    }

    public companion object {
        public const val TYPE_KEY: String = "type"
        public const val URL_KEY: String = "url"
    }
}
