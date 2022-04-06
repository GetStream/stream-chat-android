/*
 * Copyright (c) 2014-2022 Stream.io Inc. All rights reserved.
 *
 * Licensed under the Stream License;
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    https://github.com/GetStream/stream-chat-android/blob/main/LICENSE
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
 
package io.getstream.chat.android.ui.message.list.adapter.viewholder.decorator.internal

import androidx.core.view.isVisible
import com.getstream.sdk.chat.adapter.MessageListItem
import io.getstream.chat.android.ui.avatar.AvatarView
import io.getstream.chat.android.ui.message.list.DefaultShowAvatarPredicate
import io.getstream.chat.android.ui.message.list.MessageListView
import io.getstream.chat.android.ui.message.list.adapter.viewholder.internal.CustomAttachmentsViewHolder
import io.getstream.chat.android.ui.message.list.adapter.viewholder.internal.FileAttachmentsViewHolder
import io.getstream.chat.android.ui.message.list.adapter.viewholder.internal.GiphyAttachmentViewHolder
import io.getstream.chat.android.ui.message.list.adapter.viewholder.internal.GiphyViewHolder
import io.getstream.chat.android.ui.message.list.adapter.viewholder.internal.ImageAttachmentViewHolder
import io.getstream.chat.android.ui.message.list.adapter.viewholder.internal.LinkAttachmentsViewHolder
import io.getstream.chat.android.ui.message.list.adapter.viewholder.internal.MessageDeletedViewHolder
import io.getstream.chat.android.ui.message.list.adapter.viewholder.internal.MessagePlainTextViewHolder

internal class AvatarDecorator(
    private val showAvatarPredicate: MessageListView.ShowAvatarPredicate = DefaultShowAvatarPredicate(),
) : BaseDecorator() {

    /**
     * Decorates the avatar of the custom attachments message, based on the message owner.
     *
     * @param viewHolder The holder to decorate.
     * @param data The item that holds all the information.
     */
    override fun decorateCustomAttachmentsMessage(
        viewHolder: CustomAttachmentsViewHolder,
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
     * Decorates the avatar of the file attachments message, based on the message owner.
     *
     * @param viewHolder The holder to decorate.
     * @param data The item that holds all the information.
     */
    override fun decorateFileAttachmentsMessage(
        viewHolder: FileAttachmentsViewHolder,
        data: MessageListItem.MessageItem,
    ) {
        controlVisibility(viewHolder.binding.avatarMineView, viewHolder.binding.avatarView, data.isMine)
        setupAvatar(getAvatarView(viewHolder.binding.avatarMineView, viewHolder.binding.avatarView, data.isMine), data)
    }

    /**
     * Decorates the avatar of the image attachments message, based on the message owner.
     *
     * @param viewHolder The holder to decorate.
     * @param data The item that holds all the information.
     */
    override fun decorateImageAttachmentsMessage(
        viewHolder: ImageAttachmentViewHolder,
        data: MessageListItem.MessageItem,
    ) {
        controlVisibility(viewHolder.binding.avatarMineView, viewHolder.binding.avatarView, data.isMine)
        setupAvatar(getAvatarView(viewHolder.binding.avatarMineView, viewHolder.binding.avatarView, data.isMine), data)
    }

    /**
     * Decorates the avatar of the plain text message, based on the message owner.
     *
     * @param viewHolder The holder to decorate.
     * @param data The item that holds all the information.
     */
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

    /**
     * Does nothing for ephemeral Giphy message, as it doesn't contain an avatar.
     */
    override fun decorateGiphyMessage(
        viewHolder: GiphyViewHolder,
        data: MessageListItem.MessageItem,
    ) = Unit

    /**
     * Decorates the avatar of the deleted message, based on the message owner.
     *
     * @param viewHolder The holder to decorate.
     * @param data The item that holds all the information.
     */
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
