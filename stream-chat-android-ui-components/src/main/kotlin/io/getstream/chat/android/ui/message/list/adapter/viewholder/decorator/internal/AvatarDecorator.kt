package io.getstream.chat.android.ui.message.list.adapter.viewholder.decorator.internal

import androidx.core.view.isVisible
import com.getstream.sdk.chat.adapter.MessageListItem
import io.getstream.chat.android.ui.avatar.AvatarView
import io.getstream.chat.android.ui.message.list.DefaultShowAvatarPredicate
import io.getstream.chat.android.ui.message.list.MessageListView
import io.getstream.chat.android.ui.message.list.adapter.viewholder.internal.GiphyAttachmentViewHolder
import io.getstream.chat.android.ui.message.list.adapter.viewholder.internal.GiphyViewHolder
import io.getstream.chat.android.ui.message.list.adapter.viewholder.internal.ImageAttachmentViewHolder
import io.getstream.chat.android.ui.message.list.adapter.viewholder.internal.LinkAttachmentsViewHolder
import io.getstream.chat.android.ui.message.list.adapter.viewholder.internal.MessageDeletedViewHolder
import io.getstream.chat.android.ui.message.list.adapter.viewholder.internal.MessagePlainTextViewHolder
import io.getstream.chat.android.ui.message.list.adapter.viewholder.internal.TextAndAttachmentsViewHolder

internal class AvatarDecorator(
    private val showAvatarPredicate: MessageListView.ShowAvatarPredicate = DefaultShowAvatarPredicate(),
) : BaseDecorator() {

    override fun decorateTextAndAttachmentsMessage(
        viewHolder: TextAndAttachmentsViewHolder,
        data: MessageListItem.MessageItem,
    ) {
        controlVisibility(viewHolder.binding.avatarMineView, viewHolder.binding.avatarView, data.isMine)
        setupAvatar(getAvatarView(viewHolder.binding.avatarMineView, viewHolder.binding.avatarView, data.isMine), data)
    }

    /**
     * Decorates the avatar of the Giphy attachment, based on the message owner.
     *
     * @param viewHolder The holder to decorate.
     * @param data The item that holds all the information.
     */
    override fun decorateGiphyAttachmentMessage(
        viewHolder: GiphyAttachmentViewHolder,
        data: MessageListItem.MessageItem,
    ) {
        controlVisibility(viewHolder.binding.avatarMineView, viewHolder.binding.avatarView, data.isMine)
        setupAvatar(getAvatarView(viewHolder.binding.avatarMineView, viewHolder.binding.avatarView, data.isMine), data)
    }

    /**
     * Decorates the avatar of the image attachment message, based on the message owner.
     *
     * @param viewHolder The holder to decorate.
     * @param data The item that holds all the information.
     */
    override fun decorateImageAttachmentMessage(
        viewHolder: ImageAttachmentViewHolder,
        data: MessageListItem.MessageItem,
    ) {
        controlVisibility(viewHolder.binding.avatarMineView, viewHolder.binding.avatarView, data.isMine)
        setupAvatar(getAvatarView(viewHolder.binding.avatarMineView, viewHolder.binding.avatarView, data.isMine), data)
    }

    override fun decoratePlainTextMessage(
        viewHolder: MessagePlainTextViewHolder,
        data: MessageListItem.MessageItem,
    ) {
        controlVisibility(viewHolder.binding.avatarMineView, viewHolder.binding.avatarView, data.isMine)
        setupAvatar(getAvatarView(viewHolder.binding.avatarMineView, viewHolder.binding.avatarView, data.isMine), data)
    }

    /**
     * Decorates the avatar of the link attachment message, based on the message owner.
     *
     * @param viewHolder The holder to decorate.
     * @param data The item that holds all the information.
     */
    override fun decorateLinkAttachmentsMessage(
        viewHolder: LinkAttachmentsViewHolder,
        data: MessageListItem.MessageItem,
    ) {
        controlVisibility(viewHolder.binding.avatarMineView, viewHolder.binding.avatarView, data.isMine)
        setupAvatar(getAvatarView(viewHolder.binding.avatarMineView, viewHolder.binding.avatarView, data.isMine), data)
    }

    override fun decorateGiphyMessage(
        viewHolder: GiphyViewHolder,
        data: MessageListItem.MessageItem,
    ) = Unit

    override fun decorateDeletedMessage(viewHolder: MessageDeletedViewHolder, data: MessageListItem.MessageItem) {
        controlVisibility(viewHolder.binding.avatarMineView, viewHolder.binding.avatarView, data.isMine)
        setupAvatar(getAvatarView(viewHolder.binding.avatarMineView, viewHolder.binding.avatarView, data.isMine), data)
    }

    private fun setupAvatar(avatarView: AvatarView, data: MessageListItem.MessageItem) {
        val shouldShow = showAvatarPredicate.shouldShow(data)

        avatarView.isVisible = shouldShow

        if (shouldShow) {
            avatarView.setUserData(data.message.user)
        }
    }

    private fun getAvatarView(myAvatar: AvatarView, theirAvatar: AvatarView, isMine: Boolean): AvatarView {
        return if (isMine) myAvatar else theirAvatar
    }

    private fun controlVisibility(myAvatar: AvatarView, theirAvatar: AvatarView, isMine: Boolean) {
        theirAvatar.isVisible = !isMine
        myAvatar.isVisible = isMine
    }
}
