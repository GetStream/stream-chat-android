package io.getstream.chat.android.ui.messages.adapter

import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import com.getstream.sdk.chat.adapter.MessageListItem

internal class MessageListItemAdapter(
    private val viewHolderFactory: MessageListItemViewHolderFactory,
) : ListAdapter<MessageListItem, BaseMessageItemViewHolder<out MessageListItem>>(MessageListItemDiffCallback) {

    var isThread: Boolean = false

    override fun getItemId(position: Int): Long = getItem(position).getStableId()

    override fun getItemViewType(position: Int): Int {
        return viewHolderFactory.getItemViewType(getItem(position))
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int,
    ): BaseMessageItemViewHolder<out MessageListItem> {
        return viewHolderFactory.createViewHolder(parent, viewType)
    }

    override fun onBindViewHolder(holder: BaseMessageItemViewHolder<out MessageListItem>, position: Int) {
        holder.bindListItem(getItem(position), FULL_MESSAGE_LIST_ITEM_PAYLOAD_DIFF)
    }

    override fun onBindViewHolder(
        holder: BaseMessageItemViewHolder<out MessageListItem>,
        position: Int,
        payloads: MutableList<Any>,
    ) {
        val diff = (
            payloads
                .filterIsInstance<MessageListItemPayloadDiff>()
                .takeIf { it.isNotEmpty() }
                ?: listOf(FULL_MESSAGE_LIST_ITEM_PAYLOAD_DIFF)
            )
            .fold(EMPTY_MESSAGE_LIST_ITEM_PAYLOAD_DIFF) { acc, messageListItemPayloadDiff ->
                acc + messageListItemPayloadDiff
            }

        holder.bindListItem(getItem(position), diff)
    }

    override fun onViewRecycled(holder: BaseMessageItemViewHolder<out MessageListItem>) {
        super.onViewRecycled(holder)
        holder.unbind()
    }

    companion object {
        private val FULL_MESSAGE_LIST_ITEM_PAYLOAD_DIFF = MessageListItemPayloadDiff(
            text = true,
            reactions = true,
            attachments = true,
            replies = true,
            syncStatus = true,
            deleted = true,
            positions = true,
        )
        private val EMPTY_MESSAGE_LIST_ITEM_PAYLOAD_DIFF = MessageListItemPayloadDiff(
            text = false,
            reactions = false,
            attachments = false,
            replies = false,
            syncStatus = false,
            deleted = false,
            positions = false,
        )
    }
}
