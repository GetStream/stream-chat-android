package io.getstream.chat.android.ui.messages.adapter.viewholder

import android.view.ViewGroup
import com.getstream.sdk.chat.adapter.MessageListItem
import com.getstream.sdk.chat.utils.extensions.inflater
import io.getstream.chat.android.client.uploader.ProgressTracker
import io.getstream.chat.android.ui.databinding.StreamUiItemMessageEphemeralProgressBinding
import io.getstream.chat.android.ui.messages.adapter.DecoratedBaseMessageItemViewHolder
import io.getstream.chat.android.ui.messages.adapter.MessageListItemPayloadDiff
import io.getstream.chat.android.ui.messages.adapter.MessageListListenerContainer
import io.getstream.chat.android.ui.messages.adapter.viewholder.decorator.Decorator

internal class AttachmentsProgressViewHolder(
    parent: ViewGroup,
    decorators: List<Decorator>,
    listeners: MessageListListenerContainer,
    internal val binding: StreamUiItemMessageEphemeralProgressBinding =
        StreamUiItemMessageEphemeralProgressBinding.inflate(
            parent.inflater,
            parent,
            false
        ),
) : DecoratedBaseMessageItemViewHolder<MessageListItem.MessageItem>(binding.root, decorators) {

    init {
        binding.run {
            root.setOnLongClickListener {
                listeners.messageLongClickListener.onMessageLongClick(data.message)
                true
            }

        }
    }

    override fun bindData(data: MessageListItem.MessageItem, diff: MessageListItemPayloadDiff?) {
        super.bindData(data, diff)

        with(binding) {
            val id = data.message.attachments[0].uploadId

            this.sentFiles.text = data.message.text ?: "Progress"

            id?.let(ProgressTracker::getProgress)?.observeForever { progress ->
                val message = if (progress == 100) {
                    "Upload complete, processing..."
                } else {
                    "$progress%"
                }

                this.progress.text = message
            }
        }
    }
}
