package io.getstream.chat.android.ui.message.list.adapter.viewholder.decorator.internal

import androidx.core.view.isVisible
import com.getstream.sdk.chat.adapter.MessageListItem
import com.getstream.sdk.chat.utils.extensions.isBottomPosition
import io.getstream.chat.android.ui.avatar.AvatarView
import io.getstream.chat.android.ui.message.list.adapter.viewholder.internal.GiphyViewHolder
import io.getstream.chat.android.ui.message.list.adapter.viewholder.internal.MessagePlainTextViewHolder
import io.getstream.chat.android.ui.message.list.adapter.viewholder.internal.OnlyFileAttachmentsViewHolder
import io.getstream.chat.android.ui.message.list.adapter.viewholder.internal.OnlyMediaAttachmentsViewHolder
import io.getstream.chat.android.ui.message.list.adapter.viewholder.internal.PlainTextWithFileAttachmentsViewHolder
import io.getstream.chat.android.ui.message.list.adapter.viewholder.internal.PlainTextWithMediaAttachmentsViewHolder

internal class AvatarDecorator : BaseDecorator() {
    override fun decorateOnlyMediaAttachmentsMessage(
        viewHolder: OnlyMediaAttachmentsViewHolder,
        data: MessageListItem.MessageItem,
    ) {
        setupAvatar(viewHolder.binding.avatarView, data)
    }

    override fun decoratePlainTextMessage(
        viewHolder: MessagePlainTextViewHolder,
        data: MessageListItem.MessageItem,
    ) {
        setupAvatar(viewHolder.binding.avatarView, data)
    }

    override fun decoratePlainTextWithMediaAttachmentsMessage(
        viewHolder: PlainTextWithMediaAttachmentsViewHolder,
        data: MessageListItem.MessageItem,
    ) {
        setupAvatar(viewHolder.binding.avatarView, data)
    }

    override fun decorateOnlyFileAttachmentsMessage(
        viewHolder: OnlyFileAttachmentsViewHolder,
        data: MessageListItem.MessageItem,
    ) {
        setupAvatar(viewHolder.binding.avatarView, data)
    }

    override fun decoratePlainTextWithFileAttachmentsMessage(
        viewHolder: PlainTextWithFileAttachmentsViewHolder,
        data: MessageListItem.MessageItem,
    ) {
        setupAvatar(viewHolder.binding.avatarView, data)
    }

    override fun decorateGiphyMessage(
        viewHolder: GiphyViewHolder,
        data: MessageListItem.MessageItem,
    ) = Unit

    private fun setupAvatar(avatarView: AvatarView, data: MessageListItem.MessageItem) {
        if (data.isTheirs && data.isTheirs && data.isBottomPosition()) {
            avatarView.isVisible = true
            avatarView.setUserData(data.message.user)
        } else {
            avatarView.isVisible = false
        }
    }
}
