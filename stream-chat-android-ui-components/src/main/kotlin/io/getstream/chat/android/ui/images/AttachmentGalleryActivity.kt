package io.getstream.chat.android.ui.images

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.format.DateUtils
import androidx.appcompat.app.AppCompatActivity
import com.getstream.sdk.chat.utils.DateFormatter
import com.getstream.sdk.chat.utils.formatTime
import io.getstream.chat.android.ui.R
import io.getstream.chat.android.ui.databinding.StreamUiActivityAttachmentGalleryBinding
import java.util.Date
import java.util.Locale

public class AttachmentGalleryActivity : AppCompatActivity() {

    private lateinit var binding: StreamUiActivityAttachmentGalleryBinding

    private lateinit var dateFormatter: DateFormatter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        dateFormatter = DateFormatter.from(this)
        binding = StreamUiActivityAttachmentGalleryBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }

    override fun onStart() {
        super.onStart()
        val urls = intent.getStringArrayExtra(EXTRA_KEY_URLS)?.toList().orEmpty()
        val currentIndex = intent.getIntExtra(EXTRA_KEY_CURRENT_INDEX, 0)
        binding.attachmentGallery.provideImageList(this, urls, currentIndex)
        binding.run {
            closeButton.setOnClickListener { this@AttachmentGalleryActivity.onBackPressed() }
            title.text = intent.getStringExtra(EXTRA_KEY_USER_NAME)
            subtitle.text = subtitle(intent.getLongExtra(EXTRA_KEY_TIME, 0))
        }
    }

    private fun subtitle(time: Long): String {
        val relativeDay = DateUtils.getRelativeTimeSpanString(
            time,
            System.currentTimeMillis(),
            DateUtils.DAY_IN_MILLIS,
            DateUtils.FORMAT_ABBREV_RELATIVE
        )
            .toString()
            .decapitalize()

        return getString(
            R.string.stream_ui_date_and_time_pattern,
            relativeDay,
            dateFormatter.formatTime(Date(time))
        )
    }

    public companion object {
        private const val EXTRA_KEY_URLS: String = "extra_key_urls"
        private const val EXTRA_KEY_CURRENT_INDEX: String = "extra_key_current_index"
        private const val EXTRA_KEY_USER_NAME = "extra_key_user_name"
        private const val EXTRA_KEY_TIME = "extra_key_time"

        public fun createIntent(
            context: Context,
            userName: String,
            time: Long,
            currentIndex: Int,
            urls: List<String>
        ): Intent {
            return Intent(context, AttachmentGalleryActivity::class.java).apply {
                putExtra(EXTRA_KEY_CURRENT_INDEX, currentIndex)
                putExtra(EXTRA_KEY_URLS, urls.toTypedArray())
                putExtra(EXTRA_KEY_TIME, time)
                putExtra(EXTRA_KEY_USER_NAME, userName)
            }
        }
    }
}
