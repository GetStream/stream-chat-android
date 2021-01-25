package io.getstream.chat.android.chat.navigation

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContract
import androidx.activity.result.launch
import androidx.appcompat.app.AppCompatActivity
import com.getstream.sdk.chat.model.ModelType
import com.getstream.sdk.chat.navigation.destinations.AttachmentDestination
import io.getstream.chat.android.client.models.Attachment
import io.getstream.chat.android.client.models.Message
import io.getstream.chat.android.livedata.ChatDomain
import io.getstream.chat.android.ui.images.AttachmentGalleryActivity
import io.getstream.chat.android.ui.images.AttachmentGalleryActivity.Companion.EXTRA_ATTACHMENT_OPTION_RESULT
import io.getstream.chat.android.ui.utils.extensions.getCreatedAtOrThrow

public class GalleryImageAttachmentDestination(
    message: Message,
    attachment: Attachment,
    context: Context,
    private val attachmentReplyOptionHandler: AttachmentGalleryActivity.AttachmentReplyOptionHandler,
    private val attachmentShowInChatOptionHandler: AttachmentGalleryActivity.AttachmentShowInChatOptionHandler,
    private val attachmentDownloadOptionHandler: AttachmentGalleryActivity.AttachmentDownloadOptionHandler,
    private val onAttachmentDeleteOptionClickHandler: AttachmentGalleryActivity.AttachmentDeleteOptionHandler,
) : AttachmentDestination(message, attachment, context) {
    override fun showImageViewer(message: Message, attachment: Attachment) {
        val attachments: List<Attachment> =
            message.attachments.filter { it.type == ModelType.attach_image && !it.imageUrl.isNullOrEmpty() }

        if (attachments.isEmpty()) {
            Toast.makeText(context, "Invalid image(s)!", Toast.LENGTH_SHORT).show()
            return
        }

        val createdAt: Long = message.getCreatedAtOrThrow().time
        val attachmentIndex = message.attachments.indexOf(attachment)

        check(context is AppCompatActivity)
        val launcher = context.activityResultRegistry.register(
            "attachment_gallery_launcher",
            AttachmentOptionSelect(
                createdAt = createdAt,
                attachmentIndex = attachmentIndex,
                message = message,
                attachments = attachments
            )
        ) { result: AttachmentGalleryActivity.AttachmentOptionResult? ->
            result?.apply {
                when (this) {
                    is AttachmentGalleryActivity.AttachmentOptionResult.Reply -> {
                        attachmentReplyOptionHandler.onClick(this.data)
                    }
                    is AttachmentGalleryActivity.AttachmentOptionResult.ShowInChat -> {
                        attachmentShowInChatOptionHandler.onClick(this.data)
                    }
                    is AttachmentGalleryActivity.AttachmentOptionResult.Delete -> {
                        onAttachmentDeleteOptionClickHandler.onClick(this.data)
                    }
                    is AttachmentGalleryActivity.AttachmentOptionResult.Download -> {
                        attachmentDownloadOptionHandler.onClick(this.data)
                    }
                }
            }
        }
        launcher.launch()
    }
}

private class AttachmentOptionSelect(
    val createdAt: Long,
    val attachmentIndex: Int,
    val message: Message,
    val attachments: List<Attachment>,
) : ActivityResultContract<Unit, AttachmentGalleryActivity.AttachmentOptionResult?>() {

    override fun createIntent(context: Context, input: Unit?): Intent =
        AttachmentGalleryActivity.createIntent(
            context,
            createdAt,
            attachmentIndex,
            message,
            attachments,
            message.isMine(),
        )

    override fun parseResult(resultCode: Int, result: Intent?): AttachmentGalleryActivity.AttachmentOptionResult? {
        if (resultCode != Activity.RESULT_OK) {
            return null
        }
        return result?.getParcelableExtra(EXTRA_ATTACHMENT_OPTION_RESULT)
    }

    private fun Message.isMine(): Boolean {
        return ChatDomain.instance().currentUser.id == this.user.id
    }
}
