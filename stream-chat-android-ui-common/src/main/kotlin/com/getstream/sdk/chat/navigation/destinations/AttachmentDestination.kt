package com.getstream.sdk.chat.navigation.destinations

import android.content.Context
import android.content.Intent
import android.widget.Toast
import com.getstream.sdk.chat.model.ModelType
import com.getstream.sdk.chat.utils.frescoimageviewer.ImageViewer
import com.getstream.sdk.chat.view.activity.AttachmentActivity
import com.getstream.sdk.chat.view.activity.AttachmentDocumentActivity
import com.getstream.sdk.chat.view.activity.AttachmentMediaActivity
import io.getstream.chat.android.client.logger.ChatLogger
import io.getstream.chat.android.client.models.Attachment
import io.getstream.chat.android.client.models.Message
import io.getstream.chat.android.ui.common.R

public open class AttachmentDestination(
    public var message: Message,
    public var attachment: Attachment,
    context: Context,
) : ChatDestination(context) {

    override fun navigate() {
        showAttachment(message, attachment)
    }

    public fun showAttachment(message: Message, attachment: Attachment) {
        var url: String? = null
        var type: String? = attachment.type

        when (attachment.type) {
            ModelType.attach_file -> {
                loadFile(attachment)
                return
            }
            ModelType.attach_image -> {
                when {
                    attachment.ogUrl != null -> {
                        url = attachment.ogUrl
                        type = ModelType.attach_link
                    }
                    attachment.isGif() -> {
                        url = attachment.imageUrl
                        type = ModelType.attach_giphy
                    }
                    else -> {
                        showImageViewer(message, attachment)
                        return
                    }
                }
            }
            ModelType.attach_video -> url = attachment.assetUrl
            ModelType.attach_giphy -> url = attachment.thumbUrl
            ModelType.attach_product -> url = attachment.url
        }

        if (url.isNullOrEmpty()) {
            Toast.makeText(context, context.getString(R.string.stream_attachment_invalid_url), Toast.LENGTH_SHORT)
                .show()
            return
        }

        val intent = Intent(context, AttachmentActivity::class.java).apply {
            putExtra("type", type)
            putExtra("url", url)
        }
        start(intent)
    }

    private fun loadFile(attachment: Attachment) {
        val mimeType = attachment.mimeType
        val url = attachment.assetUrl

        if (mimeType == null) {
            ChatLogger.instance.logE("AttachmentDestination", "MimeType is null for url $url")
            Toast.makeText(
                context,
                context.getString(R.string.stream_attachment_invalid_mime_type, attachment.name),
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        // Media
        when {
            mimeType.contains("audio") || mimeType.contains("video") -> {
                val intent = Intent(context, AttachmentMediaActivity::class.java).apply {
                    putExtra(AttachmentMediaActivity.TYPE_KEY, mimeType)
                    putExtra(AttachmentMediaActivity.URL_KEY, url)
                }
                start(intent)
            }
            mimeType == ModelType.attach_mime_doc ||
                mimeType == ModelType.attach_mime_txt ||
                mimeType == ModelType.attach_mime_pdf ||
                mimeType == ModelType.attach_mime_html ||
                mimeType.contains("application/vnd") -> {
                val intent = Intent(context, AttachmentDocumentActivity::class.java).apply {
                    putExtra("url", url)
                }
                start(intent)
            }
        }
    }

    protected open fun showImageViewer(
        message: Message,
        attachment: Attachment,
    ) {
        val imageUrls: List<String> = message.attachments
            .filter { it.type == ModelType.attach_image && !it.imageUrl.isNullOrEmpty() }
            .mapNotNull(Attachment::imageUrl)

        if (imageUrls.isEmpty()) {
            Toast.makeText(context, "Invalid image(s)!", Toast.LENGTH_SHORT).show()
            return
        }

        val attachmentIndex = message.attachments.indexOf(attachment)

        ImageViewer.Builder(context, imageUrls)
            .setStartPosition(
                if (attachmentIndex in imageUrls.indices) attachmentIndex else 0
            )
            .show()
    }

    private fun Attachment.isGif() = mimeType?.contains("gif") ?: false
}
