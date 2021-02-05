package io.getstream.chat.android.ui.messages.adapter.viewholder.decorator

import com.getstream.sdk.chat.adapter.MessageListItem
import io.getstream.chat.android.ui.messages.adapter.BaseMessageItemViewHolder

internal interface Decorator {

    fun <T : MessageListItem> decorate(
        viewHolder: BaseMessageItemViewHolder<T>,
        data: T,
    )
}
