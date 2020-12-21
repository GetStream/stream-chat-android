package io.getstream.chat.android.ui.messages.adapter.viewholder.decorator

import android.view.View
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.isVisible
import androidx.core.view.updateLayoutParams
import com.getstream.sdk.chat.adapter.MessageListItem
import io.getstream.chat.android.ui.avatar.AvatarView
import io.getstream.chat.android.ui.messages.adapter.viewholder.MessagePlainTextViewHolder
import io.getstream.chat.android.ui.messages.adapter.viewholder.OnlyFileAttachmentsViewHolder
import io.getstream.chat.android.ui.messages.adapter.viewholder.OnlyMediaAttachmentsViewHolder
import io.getstream.chat.android.ui.messages.adapter.viewholder.PlainTextWithMediaAttachmentsViewHolder
import io.getstream.chat.android.ui.utils.extensions.dpToPx

internal class AvatarDecorator : BaseDecorator() {
    override fun decorateOnlyMediaAttachmentsMessage(
        viewHolder: OnlyMediaAttachmentsViewHolder,
        data: MessageListItem.MessageItem
    ) {
        setupAvatar(viewHolder.binding.avatarView, viewHolder.binding.mediaAttachmentsGroupView, data)
    }

    override fun decoratePlainTextMessage(viewHolder: MessagePlainTextViewHolder, data: MessageListItem.MessageItem) {
        setupAvatar(viewHolder.binding.avatarView, viewHolder.binding.messageText, data)
    }

    override fun decoratePlainTextWithMediaAttachmentsMessage(
        viewHolder: PlainTextWithMediaAttachmentsViewHolder,
        data: MessageListItem.MessageItem
    ) {
        setupAvatar(viewHolder.binding.avatarView, viewHolder.binding.mediaAttachmentsGroupView, data)
    }

    override fun decorateOnlyFileAttachmentsMessage(
        viewHolder: OnlyFileAttachmentsViewHolder,
        data: MessageListItem.MessageItem
    ) {
        setupAvatar(viewHolder.binding.avatarView, viewHolder.binding.fileAttachmentsView, data)
    }

    private fun setupAvatar(avatarView: AvatarView, viewNextFromAvatar: View, data: MessageListItem.MessageItem) {
        if (data.isTheirs) {
            avatarView.setUserData(data.message.user)
        }
        viewNextFromAvatar.updateLayoutParams<ConstraintLayout.LayoutParams> {
            marginStart = 0
        }
        when {
            data.isTheirs && data.positions.contains(MessageListItem.Position.BOTTOM) -> avatarView.isVisible = true
            data.isTheirs -> {
                avatarView.isVisible = false
                viewNextFromAvatar.updateLayoutParams<ConstraintLayout.LayoutParams> { marginStart = AVATAR_SIDE_SPACE }
            }
            else -> avatarView.isVisible = false
        }
    }

    companion object {
        private val AVATAR_SIDE_SPACE = 48.dpToPx()
    }
}
