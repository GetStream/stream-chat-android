package io.getstream.chat.android.chat.navigation

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.ActivityResultRegistry
import androidx.activity.result.contract.ActivityResultContract
import com.getstream.sdk.chat.model.ModelType
import com.getstream.sdk.chat.navigation.destinations.AttachmentDestination
import io.getstream.chat.android.chat.navigation.AttachmentOptionSelect.Input
import io.getstream.chat.android.client.models.Attachment
import io.getstream.chat.android.client.models.Message
import io.getstream.chat.android.livedata.ChatDomain
import io.getstream.chat.android.ui.gallery.AttachmentGalleryActivity
import io.getstream.chat.android.ui.gallery.AttachmentGalleryActivity.Companion.EXTRA_ATTACHMENT_OPTION_RESULT
import io.getstream.chat.android.ui.utils.extensions.getCreatedAtOrThrow

public class GalleryImageAttachmentDestination(
    message: Message,
    attachment: Attachment,
    context: Context,
    private val attachmentReplyOptionHandler: AttachmentGalleryActivity.AttachmentReplyOptionHandler,
    private val attachmentShowInChatOptionHandler: AttachmentGalleryActivity.AttachmentShowInChatOptionHandler,
    private val attachmentDownloadOptionHandler: AttachmentGalleryActivity.AttachmentDownloadOptionHandler,
    private val attachmentDeleteOptionClickHandler: AttachmentGalleryActivity.AttachmentDeleteOptionHandler,
) : AttachmentDestination(message, attachment, context) {

    private var launcher: ActivityResultLauncher<Input>? = null

    internal fun register(
        activityResultRegistry: ActivityResultRegistry,
    ) {
        launcher = activityResultRegistry.register(
            "attachment_gallery_launcher",
            AttachmentOptionSelect()
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
                        attachmentDeleteOptionClickHandler.onClick(this.data)
                    }
                    is AttachmentGalleryActivity.AttachmentOptionResult.Download -> {
                        attachmentDownloadOptionHandler.onClick(this.data)
                    }
                }
            }
        }
    }

    internal fun unregister() {
        launcher?.unregister()
        launcher = null
    }

    override fun showImageViewer(message: Message, attachment: Attachment) {
        val attachments: List<Attachment> =
            message.attachments.filter { it.type == ModelType.attach_image && !it.imageUrl.isNullOrEmpty() }

        if (attachments.isEmpty()) {
            Toast.makeText(context, "Invalid image(s)!", Toast.LENGTH_SHORT).show()
            return
        }

        val createdAt: Long = message.getCreatedAtOrThrow().time
        val attachmentIndex = message.attachments.indexOf(attachment)

        launcher?.launch(
            Input(
                createdAt = createdAt,
                attachmentIndex = attachmentIndex,
                message = message,
                attachments = attachments
            )
        )
    }
}

private class AttachmentOptionSelect :
    ActivityResultContract<Input, AttachmentGalleryActivity.AttachmentOptionResult?>() {

    class Input(
        val createdAt: Long,
        val attachmentIndex: Int,
        val message: Message,
        val attachments: List<Attachment>,
    )

    override fun createIntent(context: Context, input: Input): Intent =
        AttachmentGalleryActivity.createIntent(
            context,
            input.createdAt,
            input.attachmentIndex,
            input.message,
            input.attachments,
            input.message.isMine(),
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
