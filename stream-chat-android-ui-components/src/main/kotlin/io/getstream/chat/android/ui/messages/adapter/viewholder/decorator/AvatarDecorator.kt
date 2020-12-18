package io.getstream.chat.android.ui.messages.adapter.viewholder.decorator

import android.view.View
import com.getstream.sdk.chat.adapter.MessageListItem
import io.getstream.chat.android.ui.avatar.AvatarView
import io.getstream.chat.android.ui.messages.adapter.viewholder.MessagePlainTextViewHolder
import io.getstream.chat.android.ui.messages.adapter.viewholder.OnlyFileAttachmentsViewHolder
import io.getstream.chat.android.ui.messages.adapter.viewholder.OnlyMediaAttachmentsViewHolder
import io.getstream.chat.android.ui.messages.adapter.viewholder.PlainTextWithMediaAttachmentsViewHolder

internal class AvatarDecorator : BaseDecorator() {
    override fun decorateOnlyMediaAttachmentsMessage(
        viewHolder: OnlyMediaAttachmentsViewHolder,
        data: MessageListItem.MessageItem
    ) {
        setupAvatar(viewHolder.binding.avatarView, data)
    }

    override fun decoratePlainTextMessage(viewHolder: MessagePlainTextViewHolder, data: MessageListItem.MessageItem) {
        setupAvatar(viewHolder.binding.avatarView, data)
    }

    override fun decoratePlainTextWithMediaAttachmentsMessage(
        viewHolder: PlainTextWithMediaAttachmentsViewHolder,
        data: MessageListItem.MessageItem
    ) {
        setupAvatar(viewHolder.binding.avatarView, data)
    }

    override fun decorateOnlyFileAttachmentsMessage(
        viewHolder: OnlyFileAttachmentsViewHolder,
        data: MessageListItem.MessageItem
    ) {
        setupAvatar(viewHolder.binding.avatarView, data)
    }

    private fun setupAvatar(avatarView: AvatarView, data: MessageListItem.MessageItem) {
        if (data.isTheirs) {
            avatarView.setUserData(data.message.user)
        }
        avatarView.visibility = when {
            data.isTheirs && data.positions.contains(MessageListItem.Position.BOTTOM) -> View.VISIBLE
            data.isTheirs -> View.INVISIBLE
            else -> View.GONE
        }
    }
}
