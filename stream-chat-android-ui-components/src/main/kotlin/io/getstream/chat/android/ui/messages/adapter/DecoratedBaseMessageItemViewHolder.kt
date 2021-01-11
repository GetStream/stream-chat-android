package io.getstream.chat.android.ui.messages.adapter

import android.view.View
import androidx.annotation.CallSuper
import com.getstream.sdk.chat.adapter.MessageListItem
import io.getstream.chat.android.ui.messages.adapter.viewholder.decorator.Decorator

internal abstract class DecoratedBaseMessageItemViewHolder<T : MessageListItem>(
    itemView: View,
    private val decorators: List<Decorator>,
) : BaseMessageItemViewHolder<T>(itemView) {
    @CallSuper
    override fun bindData(data: T, diff: MessageListItemPayloadDiff?) {
        decorators.forEach { it.decorate(this, data) }
    }
}
