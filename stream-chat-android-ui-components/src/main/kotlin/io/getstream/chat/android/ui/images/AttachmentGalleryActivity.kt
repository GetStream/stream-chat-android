package io.getstream.chat.android.ui.images

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import io.getstream.chat.android.ui.databinding.StreamUiActivityAttachmentGalleryBinding

public class AttachmentGalleryActivity : AppCompatActivity() {

    private lateinit var binding: StreamUiActivityAttachmentGalleryBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = StreamUiActivityAttachmentGalleryBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }

    override fun onStart() {
        super.onStart()
        val urls = intent.getStringArrayExtra(EXTRA_KEY_URLS)?.toList().orEmpty()
        val currentIndex = intent.getIntExtra(EXTRA_KEY_CURRENT_INDEX, 0)
        binding.attachmentGallery.provideImageList(this, urls, currentIndex)
    }

    public companion object {
        private const val EXTRA_KEY_URLS: String = "extra_key_urls"
        private const val EXTRA_KEY_CURRENT_INDEX: String = "extra_key_current_index"

        public fun createIntent(context: Context, currentIndex: Int, urls: List<String>): Intent {
            return Intent(context, AttachmentGalleryActivity::class.java).apply {
                putExtra(EXTRA_KEY_CURRENT_INDEX, currentIndex)
                putExtra(EXTRA_KEY_URLS, urls.toTypedArray())
            }
        }
    }
}