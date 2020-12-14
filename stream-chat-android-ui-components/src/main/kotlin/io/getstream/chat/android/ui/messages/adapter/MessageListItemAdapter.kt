package io.getstream.chat.android.ui.messages.adapter

import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import com.getstream.sdk.chat.adapter.MessageListItem
import com.getstream.sdk.chat.adapter.MessageListItemDiffCallback
import com.getstream.sdk.chat.adapter.MessageListItemPayloadDiff

public class MessageListItemAdapter(
    private val viewHolderFactory: MessageListItemViewHolderFactory
) : ListAdapter<MessageListItem, BaseMessageItemViewHolder<*>>(MessageListItemDiffCallback) {

    public var isThread: Boolean = false

    override fun getItemId(position: Int): Long = getItem(position).getStableId()

    override fun getItemViewType(position: Int): Int {
        return MessageListItemViewTypeMapper.getViewTypeValue(getItem(position))
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): BaseMessageItemViewHolder<*> {
        return viewHolderFactory.createViewHolder(parent, viewType)
    }

    override fun onBindViewHolder(holder: BaseMessageItemViewHolder<*>, position: Int) {
        holder.bindListItem(getItem(position), FULL_MESSAGE_LIST_ITEM_PAYLOAD_DIFF)
    }

    override fun onBindViewHolder(
        holder: BaseMessageItemViewHolder<*>,
        position: Int,
        payloads: MutableList<Any>
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

    public companion object {
        private val FULL_MESSAGE_LIST_ITEM_PAYLOAD_DIFF = MessageListItemPayloadDiff(
            text = true,
            reactions = true,
            attachments = true,
            replies = true,
            syncStatus = true,
            deleted = true,
            positions = true,
            readBy = true
        )
        private val EMPTY_MESSAGE_LIST_ITEM_PAYLOAD_DIFF = MessageListItemPayloadDiff(
            text = false,
            reactions = false,
            attachments = false,
            replies = false,
            syncStatus = false,
            deleted = false,
            positions = false,
            readBy = false
        )
    }
}
