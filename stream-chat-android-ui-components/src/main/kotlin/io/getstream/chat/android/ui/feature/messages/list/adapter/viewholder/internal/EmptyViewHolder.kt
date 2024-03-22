package io.getstream.chat.android.ui.feature.messages.list.adapter.viewholder.internal

import android.view.View
import android.view.ViewGroup
import io.getstream.chat.android.ui.feature.messages.list.adapter.BaseMessageItemViewHolder
import io.getstream.chat.android.ui.feature.messages.list.adapter.MessageListItem
import io.getstream.chat.android.ui.feature.messages.list.adapter.MessageListItemPayloadDiff

internal class EmptyViewHolder(
    parentView: ViewGroup,
    val viewType: Int,
) : BaseMessageItemViewHolder<MessageListItem>(View(parentView.context)) {
    override fun bindData(data: MessageListItem, diff: MessageListItemPayloadDiff?) = Unit
}