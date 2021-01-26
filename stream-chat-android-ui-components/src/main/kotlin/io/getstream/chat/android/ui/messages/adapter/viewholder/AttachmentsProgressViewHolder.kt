package io.getstream.chat.android.ui.messages.adapter.viewholder

import android.view.ViewGroup
import com.getstream.sdk.chat.adapter.MessageListItem
import com.getstream.sdk.chat.utils.extensions.inflater
import io.getstream.chat.android.client.extensions.uploadId
import io.getstream.chat.android.client.uploader.ProgressTrackerFactory
import io.getstream.chat.android.ui.databinding.StreamUiItemMessageEphemeralProgressBinding
import io.getstream.chat.android.ui.messages.adapter.BaseMessageItemViewHolder
import io.getstream.chat.android.ui.messages.adapter.MessageListItemPayloadDiff
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

internal class AttachmentsProgressViewHolder(
    parent: ViewGroup,
    internal val binding: StreamUiItemMessageEphemeralProgressBinding =
        StreamUiItemMessageEphemeralProgressBinding.inflate(
            parent.inflater,
            parent,
            false
        ),
) : BaseMessageItemViewHolder<MessageListItem.MessageItem>(binding.root) {

    override fun bindData(data: MessageListItem.MessageItem, diff: MessageListItemPayloadDiff?) {
        data.message.uploadId?.let(ProgressTrackerFactory::getOrCreate)?.let { tracker ->
            GlobalScope.launch(Dispatchers.Main) {
                tracker.lapsCompleted().collect { laps ->
                    binding.sentFiles.text = "$laps / ${tracker.getNumberOfItems()}"
                }
            }

            GlobalScope.launch(Dispatchers.Main) {
                tracker.currentItemProgress().collect { progress ->
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
