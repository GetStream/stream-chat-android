package io.getstream.chat.ui.sample.feature.chat

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import coil.load
import io.getstream.chat.android.client.models.Attachment
import io.getstream.chat.android.ui.message.list.adapter.viewholder.attachment.AttachmentViewFactory
import io.getstream.chat.ui.sample.R
import io.getstream.chat.ui.sample.databinding.ListItemAttachmentImgurBinding

class ImgurAttachmentFactory : AttachmentViewFactory() {

    override fun createLinkAttachmentView(linkAttachment: Attachment, context: Context): View {
        val imgurAttachment = linkAttachment.takeIf { it.isImgurAttachment() }
        return when {
            imgurAttachment != null -> createImgurAttachment(imgurAttachment, context)
            else -> super.createLinkAttachmentView(linkAttachment, context)
        }
    }

    override fun createAttachmentsView(attachments: List<Attachment>, context: Context): View {
        val imgurAttachment = attachments.firstOrNull { it.isImgurAttachment() }
        return when {
            imgurAttachment != null -> createImgurAttachment(imgurAttachment, context)
            else -> super.createAttachmentsView(attachments, context)
        }
    }

    private fun Attachment.isImgurAttachment(): Boolean {
        return imageUrl?.contains("imgur") == true
    }

    private fun createImgurAttachment(imgurAttachment: Attachment, context: Context): View {
        val binding = ListItemAttachmentImgurBinding.inflate(LayoutInflater.from(context), null, false)

        binding.ivMediaThumb.apply {
            shapeAppearanceModel = shapeAppearanceModel.toBuilder()
                .setAllCornerSizes(resources.getDimension(R.dimen.stream_ui_selected_attachment_corner_radius))
                .build()
            load(imgurAttachment.imageUrl) {
                allowHardware(false)
                crossfade(true)
                placeholder(R.drawable.stream_ui_picture_placeholder)
            }
        }

        return binding.root
    }
}
