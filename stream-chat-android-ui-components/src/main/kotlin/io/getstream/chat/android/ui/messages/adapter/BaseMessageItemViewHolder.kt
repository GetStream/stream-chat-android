package io.getstream.chat.android.ui.messages.adapter

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.getstream.sdk.chat.adapter.MessageListItem
import io.getstream.chat.android.ui.messages.adapter.viewholder.decorator.BackgroundDecorator
import io.getstream.chat.android.ui.messages.adapter.viewholder.decorator.Decorator
import io.getstream.chat.android.ui.messages.adapter.viewholder.decorator.GapDecorator

public abstract class BaseMessageItemViewHolder<T : MessageListItem>(itemView: View) : RecyclerView.ViewHolder(itemView) {
    private val decorators = listOf<Decorator>(BackgroundDecorator(), GapDecorator())

    public fun bind(data: T) {
        decorators.forEach { it.decorate(this, data) }
        bindData(data)
    }

    public abstract fun bindData(data: T)
}
