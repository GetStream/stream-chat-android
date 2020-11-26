package com.getstream.sdk.chat.adapter.viewholder.message

import android.content.Context
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.getstream.sdk.chat.adapter.MessageListItem
import com.getstream.sdk.chat.adapter.MessageListItemPayloadDiff

public abstract class BaseMessageListItemViewHolder<T : MessageListItem>(
    itemView: View
) : RecyclerView.ViewHolder(itemView) {

    /**
     * Workaround to allow a downcast of the MessageListItem to T
     */
    @Suppress("UNCHECKED_CAST")
    internal fun bindListItem(messageListItem: MessageListItem, diff: MessageListItemPayloadDiff? = null) = bind(messageListItem as T, diff)

    protected abstract fun bind(messageListItem: T, diff: MessageListItemPayloadDiff?)

    protected val context: Context
        get() = itemView.context
}
