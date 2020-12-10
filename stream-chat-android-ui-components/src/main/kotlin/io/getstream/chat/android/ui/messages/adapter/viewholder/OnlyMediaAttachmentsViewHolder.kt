package io.getstream.chat.android.ui.messages.adapter.viewholder

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import com.getstream.sdk.chat.ImageLoader.load
import com.getstream.sdk.chat.adapter.MessageListItem
import com.getstream.sdk.chat.adapter.MessageListItemPayloadDiff
import io.getstream.chat.android.client.models.Attachment
import io.getstream.chat.android.ui.R
import io.getstream.chat.android.ui.databinding.StreamItemMessageAttachmentsOnlyBinding
import io.getstream.chat.android.ui.messages.adapter.BaseMessageItemViewHolder

public class OnlyMediaAttachmentsViewHolder(
    parent: ViewGroup,
    internal val binding: StreamItemMessageAttachmentsOnlyBinding = StreamItemMessageAttachmentsOnlyBinding.inflate(
        LayoutInflater.from(
            parent.context
        ),
        parent,
        false
    )
) : BaseMessageItemViewHolder<MessageListItem.MessageItem>(binding.root) {

    override fun bindData(data: MessageListItem.MessageItem, diff: MessageListItemPayloadDiff?) {
        if (data.message.attachments.firstOrNull()?.type == "image") {
            showImage(data.message.attachments.first(), data.isMine)
        }
    }

    private fun showImage(imageAttachment: Attachment, isMine: Boolean) {
        constraintView(isMine, binding.backgroundView, binding.root)
        imageAttachment.imageUrl?.let { url ->
            binding.imageView.load(
                uri = url,
                placeholderResId = R.drawable.stream_ui_picture_placeholder,
                onStart = { binding.loadImage.isVisible = true },
                onComplete = { binding.loadImage.isVisible = false }
            )
        }
    }
}
