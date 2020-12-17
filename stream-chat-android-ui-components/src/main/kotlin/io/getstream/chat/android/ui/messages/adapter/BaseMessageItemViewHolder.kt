package io.getstream.chat.android.ui.messages.adapter

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.getstream.sdk.chat.adapter.MessageListItem
import io.getstream.chat.android.ui.messages.adapter.viewholder.decorator.AvatarDecorator
import io.getstream.chat.android.ui.messages.adapter.viewholder.decorator.BackgroundDecorator
import io.getstream.chat.android.ui.messages.adapter.viewholder.decorator.Decorator
import io.getstream.chat.android.ui.messages.adapter.viewholder.decorator.GapDecorator
import io.getstream.chat.android.ui.messages.adapter.viewholder.decorator.GravityDecorator
import io.getstream.chat.android.ui.messages.adapter.viewholder.decorator.MaxPossibleWidthDecorator

public abstract class BaseMessageItemViewHolder<T : MessageListItem>(
    itemView: View
) : RecyclerView.ViewHolder(itemView) {
    private val decorators = listOf<Decorator>(
        BackgroundDecorator(),
        GapDecorator(),
        MaxPossibleWidthDecorator(),
        AvatarDecorator(),
        GravityDecorator()
    )

    public fun bind(data: T, diff: MessageListItemPayloadDiff? = null) {
        decorators.forEach { it.decorate(this, data) }
        bindData(data, diff)
    }

    /**
     * Workaround to allow a downcast of the MessageListItem to T
     */
    @Suppress("UNCHECKED_CAST")
    internal fun bindListItem(messageListItem: MessageListItem, diff: MessageListItemPayloadDiff) =
        bind(messageListItem as T, diff)

    public abstract fun bindData(data: T, diff: MessageListItemPayloadDiff?)
}
