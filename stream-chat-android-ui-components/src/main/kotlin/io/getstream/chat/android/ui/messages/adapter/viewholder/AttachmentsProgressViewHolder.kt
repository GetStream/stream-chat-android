package io.getstream.chat.android.ui.messages.adapter.viewholder

import android.view.ViewGroup
import com.getstream.sdk.chat.adapter.MessageListItem
import com.getstream.sdk.chat.utils.extensions.inflater
import io.getstream.chat.android.client.uploader.ProgressTrackerFactory
import io.getstream.chat.android.ui.databinding.StreamUiItemMessageEphemeralProgressBinding
import io.getstream.chat.android.ui.messages.adapter.DecoratedBaseMessageItemViewHolder
import io.getstream.chat.android.ui.messages.adapter.MessageListItemPayloadDiff
import io.getstream.chat.android.ui.messages.adapter.MessageListListenerContainer
import io.getstream.chat.android.ui.messages.adapter.viewholder.decorator.Decorator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

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

    override fun bindData(data: MessageListItem.MessageItem, isThread: Boolean, diff: MessageListItemPayloadDiff?) {
        super.bindData(data, isThread, diff)

        data.message.uploadId?.let(ProgressTrackerFactory.Companion::getOrCreate)?.let { tracker ->
            GlobalScope.launch(Dispatchers.Main) {
                tracker.lapsCompleted().collect { laps ->
                    binding.sentFiles.text = "$laps / ${tracker.getNumberOfLaps()}"
                }
            }

            GlobalScope.launch(Dispatchers.Main) {
                tracker.currentProgress().collect { progress ->
                    val message = if (progress == 100) {
                        "Upload complete, processing..."
                    } else {
                        "$progress%"
                    }

                    binding.progress.text = message
                }
            }
        }
    }
}
