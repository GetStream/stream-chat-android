package io.getstream.chat.android.ui.images

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Parcelable
import android.text.format.DateUtils
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import com.getstream.sdk.chat.utils.DateFormatter
import com.getstream.sdk.chat.utils.formatTime
import io.getstream.chat.android.client.models.Attachment
import io.getstream.chat.android.client.models.Message
import io.getstream.chat.android.client.models.name
import io.getstream.chat.android.ui.R
import io.getstream.chat.android.ui.databinding.StreamUiActivityAttachmentGalleryBinding
import io.getstream.chat.android.ui.options.attachment.AttachmentOptionsDialogFragment
import kotlinx.parcelize.Parcelize
import java.util.Date

public class AttachmentGalleryActivity : AppCompatActivity() {

    private lateinit var binding: StreamUiActivityAttachmentGalleryBinding

    private lateinit var dateFormatter: DateFormatter

    private var isFullScreen = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        dateFormatter = DateFormatter.from(this)
        binding = StreamUiActivityAttachmentGalleryBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }

    override fun onStart() {
        super.onStart()

        val attachments = obtainAttachments()
        val attachmentUrls = attachments.mapNotNull { it.imageUrl }
        val currentIndex = intent.getIntExtra(EXTRA_KEY_CURRENT_INDEX, 0)
        binding.attachmentGallery.provideImageList(
            fragmentActivity = this,
            imageList = attachmentUrls,
            currentIndex = currentIndex,
            imageClickListener = {
                binding.toolBarGroup.isVisible = isFullScreen
                isFullScreen = !isFullScreen
            }
        )
        binding.run {
            closeButton.setOnClickListener { this@AttachmentGalleryActivity.onBackPressed() }
            title.text = intent.getStringExtra(EXTRA_KEY_USER_NAME)
            subtitle.text = subtitle(intent.getLongExtra(EXTRA_KEY_TIME, 0))
            val currentAttachment = obtainAttachments()[binding.attachmentGallery.currentItemIndex]
            menuButton.setOnClickListener {
                val deleteHandler = AttachmentOptionsDialogFragment.AttachmentOptionHandler {
                    val result = Intent().apply {
                        putExtra(EXTRA_ATTACHMENT_OPTION_RESULT, AttachmentOptionResult.Delete(currentAttachment))
                    }
                    setResult(Activity.RESULT_OK, Intent(result))
                    finish()
                }
                val saveHandler = AttachmentOptionsDialogFragment.AttachmentOptionHandler {
                    val result = Intent().apply {
                        putExtra(EXTRA_ATTACHMENT_OPTION_RESULT, AttachmentOptionResult.Download(currentAttachment))
                    }
                    setResult(Activity.RESULT_OK, Intent(result))
                    finish()
                }
                val showInChatHandler = AttachmentOptionsDialogFragment.AttachmentOptionHandler {
                    val result = Intent().apply {
                        putExtra(EXTRA_ATTACHMENT_OPTION_RESULT, AttachmentOptionResult.ShowInChat(currentAttachment))
                    }
                    setResult(Activity.RESULT_OK, Intent(result))
                    finish()
                }
                val replyHandler = AttachmentOptionsDialogFragment.AttachmentOptionHandler {
                    val result = Intent().apply {
                        putExtra(EXTRA_ATTACHMENT_OPTION_RESULT, AttachmentOptionResult.Reply(currentAttachment))
                    }
                    setResult(Activity.RESULT_OK, Intent(result))
                    finish()
                }
                AttachmentOptionsDialogFragment.newInstance(
                    showInChatHandler = showInChatHandler,
                    deleteHandler = deleteHandler,
                    replyHandler = replyHandler,
                    saveImageHandler = saveHandler,
                    isMine = currentAttachment.isMine,
                ).show(supportFragmentManager, AttachmentOptionsDialogFragment.TAG)
            }
        }
    }

    private fun obtainAttachments() =
        intent.getParcelableArrayListExtra<AttachmentData>(EXTRA_KEY_ATTACHMENTS)?.toList().orEmpty()

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
        private const val EXTRA_KEY_ATTACHMENTS: String = "extra_key_attachments"
        private const val EXTRA_KEY_CURRENT_INDEX: String = "extra_key_current_index"
        private const val EXTRA_KEY_USER_NAME = "extra_key_user_name"
        private const val EXTRA_KEY_TIME = "extra_key_time"

        internal const val EXTRA_ATTACHMENT_OPTION_RESULT = "extra_attachment_option_result"

        @JvmStatic
        public fun createIntent(
            context: Context,
            time: Long,
            currentIndex: Int,
            message: Message,
            attachments: List<Attachment>,
            isMine: Boolean,
        ): Intent {
            val userName = message.user.name
            val attachmentsData = attachments.map { it.toAttachmentData(message.id, message.cid, userName, isMine) }
            return Intent(context, AttachmentGalleryActivity::class.java).apply {
                putExtra(EXTRA_KEY_CURRENT_INDEX, currentIndex)
                putExtra(EXTRA_KEY_TIME, time)
                putExtra(EXTRA_KEY_USER_NAME, userName)
                putParcelableArrayListExtra(EXTRA_KEY_ATTACHMENTS, ArrayList(attachmentsData))
            }
        }

        private fun Attachment.toAttachmentData(
            messageId: String,
            cid: String,
            userName: String,
            isMine: Boolean,
        ): AttachmentData =
            AttachmentData(
                messageId = messageId,
                cid = cid,
                userName = userName,
                isMine = isMine,
                imageUrl = this.imageUrl,
                assetUrl = this.assetUrl,
                name = this.name
            )
    }

    @Parcelize
    public data class AttachmentData(
        val messageId: String,
        val cid: String,
        val userName: String,
        val isMine: Boolean = false,
        val authorName: String? = null,
        val imageUrl: String? = null,
        val assetUrl: String? = null,
        val mimeType: String? = null,
        val fileSize: Int = 0,
        val title: String? = null,
        val text: String? = null,
        val type: String? = null,
        val image: String? = null,
        val url: String? = null,
        val name: String? = null,
    ) : Parcelable {
        public fun toAttachment(): Attachment =
            Attachment(
                authorName = authorName,
                imageUrl = imageUrl,
                assetUrl = assetUrl,
                url = url,
                name = name,
                image = image,
                type = type,
                text = text,
                title = title,
                fileSize = fileSize,
                mimeType = mimeType,
            )
    }

    public fun interface AttachmentShowInChatOptionHandler {
        public fun onClick(attachmentData: AttachmentData): Unit
    }

    public fun interface AttachmentReplyOptionHandler {
        public fun onClick(attachmentData: AttachmentData): Unit
    }

    public fun interface AttachmentDownloadOptionHandler {
        public fun onClick(attachmentData: AttachmentData): Unit
    }

    public fun interface AttachmentDeleteOptionHandler {
        public fun onClick(attachmentData: AttachmentData): Unit
    }

    internal sealed class AttachmentOptionResult(open val data: AttachmentData) : Parcelable {
        @Parcelize
        internal class Reply(override val data: AttachmentData) : AttachmentOptionResult(data)

        @Parcelize
        internal class ShowInChat(override val data: AttachmentData) : AttachmentOptionResult(data)

        @Parcelize
        internal class Delete(override val data: AttachmentData) : AttachmentOptionResult(data)

        @Parcelize
        internal class Download(override val data: AttachmentData) : AttachmentOptionResult(data)
    }
}
