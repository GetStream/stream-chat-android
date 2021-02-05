package io.getstream.chat.android.ui.messages.adapter.viewholder.decorator

import androidx.core.view.isVisible
import com.getstream.sdk.chat.adapter.MessageListItem
import com.getstream.sdk.chat.utils.extensions.isBottomPosition
import io.getstream.chat.android.ui.avatar.AvatarView
import io.getstream.chat.android.ui.messages.adapter.viewholder.GiphyViewHolder
import io.getstream.chat.android.ui.messages.adapter.viewholder.MessagePlainTextViewHolder
import io.getstream.chat.android.ui.messages.adapter.viewholder.OnlyFileAttachmentsViewHolder
import io.getstream.chat.android.ui.messages.adapter.viewholder.OnlyMediaAttachmentsViewHolder
import io.getstream.chat.android.ui.messages.adapter.viewholder.PlainTextWithFileAttachmentsViewHolder
import io.getstream.chat.android.ui.messages.adapter.viewholder.PlainTextWithMediaAttachmentsViewHolder

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
