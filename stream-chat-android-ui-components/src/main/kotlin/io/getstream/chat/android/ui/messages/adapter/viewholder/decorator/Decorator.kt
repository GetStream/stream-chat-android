package io.getstream.chat.android.ui.messages.adapter.viewholder.decorator

import com.getstream.sdk.chat.adapter.MessageListItem
import io.getstream.chat.android.ui.messages.adapter.BaseMessageItemViewHolder

public interface Decorator {
    public fun <T : MessageListItem> decorate(viewHolder: BaseMessageItemViewHolder<T>, data: T)
}
