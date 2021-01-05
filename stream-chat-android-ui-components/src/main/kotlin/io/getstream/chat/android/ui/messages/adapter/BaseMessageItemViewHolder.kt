package io.getstream.chat.android.ui.messages.adapter

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.getstream.sdk.chat.adapter.MessageListItem
import io.getstream.chat.android.ui.messages.adapter.viewholder.decorator.Decorator

public abstract class BaseMessageItemViewHolder<T : MessageListItem>(
    itemView: View,
    private val decorators: List<Decorator>
) : RecyclerView.ViewHolder(itemView) {
    protected lateinit var data: T

    public fun bind(data: T, diff: MessageListItemPayloadDiff? = null) {
        this.data = data
        decorators.forEach { it.decorate(this, data) }
        bindData(data, diff)
    }

    /**
     * Workaround to allow a downcast of the MessageListItem to T
     */
    @Suppress("UNCHECKED_CAST")
    internal fun bindListItem(messageListItem: MessageListItem, diff: MessageListItemPayloadDiff? = null) {
        bind(messageListItem as T, diff)
    }

    public abstract fun bindData(data: T, diff: MessageListItemPayloadDiff?)
}
