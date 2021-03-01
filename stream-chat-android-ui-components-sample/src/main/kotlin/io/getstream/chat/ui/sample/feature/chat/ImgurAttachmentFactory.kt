package io.getstream.chat.ui.sample.feature.chat

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import coil.load
import com.getstream.sdk.chat.adapter.MessageListItem
import io.getstream.chat.android.client.models.Attachment
import io.getstream.chat.android.ui.message.list.adapter.MessageListListenerContainer
import io.getstream.chat.android.ui.message.list.adapter.viewholder.attachment.AttachmentViewFactory
import io.getstream.chat.android.ui.message.list.internal.MessageListItemStyle
import io.getstream.chat.ui.sample.R
import io.getstream.chat.ui.sample.databinding.ListItemAttachmentImgurBinding

class ImgurAttachmentFactory : AttachmentViewFactory() {

    override fun createAttachmentViews(
        data: MessageListItem.MessageItem,
        listeners: MessageListListenerContainer,
        style: MessageListItemStyle,
        parent: View,
    ): View {
        val imgurAttachment = data.message.attachments.firstOrNull { it.isImgurAttachment() }
        return when {
            imgurAttachment != null -> createImgurAttachment(imgurAttachment, parent.context)
            else -> super.createAttachmentViews(data, listeners, style, parent)
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
