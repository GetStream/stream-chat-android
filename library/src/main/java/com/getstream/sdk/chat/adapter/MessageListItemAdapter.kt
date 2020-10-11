package com.getstream.sdk.chat.adapter

import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import com.getstream.sdk.chat.adapter.viewholder.message.BaseMessageListItemViewHolder
import com.getstream.sdk.chat.view.MessageListViewStyle
import io.getstream.chat.android.client.models.Channel

class MessageListItemAdapter(
    private val channel: Channel,
    private val viewHolderFactory: MessageViewHolderFactory,
    private val style: MessageListViewStyle
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
        holder.bindListItem(getItem(position))
    }
}
