package com.getstream.sdk.chat.adapter

import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import com.getstream.sdk.chat.adapter.viewholder.message.BaseMessageListItemViewHolder
import com.getstream.sdk.chat.adapter.viewholder.message.MessageListItemViewHolder
import com.getstream.sdk.chat.view.MessageListViewStyle
import io.getstream.chat.android.client.models.Channel

internal class MessageListItemAdapter(
    private val channel: Channel,
    private val viewHolderFactory: MessageViewHolderFactory,
    private val style: MessageListViewStyle,
) : ListAdapter<MessageListItem, BaseMessageListItemViewHolder<*>>(MessageListItemDiffCallback) {

    var isThread = false

    @Deprecated(
        message = "Use submitList instead",
        replaceWith = ReplaceWith("submitList(newEntities)"),
        level = DeprecationLevel.ERROR
    )
    fun replaceEntities(newEntities: List<MessageListItem>) {
        submitList(newEntities)
    }

    override fun getItemId(position: Int): Long = getItem(position).getStableId()

    override fun getItemViewType(position: Int): Int {
        val messageListItem = getItem(position)
        return viewHolderFactory.getMessageViewType(messageListItem)
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): BaseMessageListItemViewHolder<*> {
        return viewHolderFactory.createMessageViewHolder(parent, viewType, style, channel)
    }

    override fun onBindViewHolder(holder: BaseMessageListItemViewHolder<*>, position: Int) {
        holder.bindListItem(getItem(position), FULL_MESSAGE_LIST_ITEM_PAYLOAD_DIFF)
    }

    override fun onBindViewHolder(holder: BaseMessageListItemViewHolder<*>, position: Int, payloads: MutableList<Any>) {
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

    companion object {
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
