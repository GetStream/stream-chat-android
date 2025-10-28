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

package io.getstream.chat.android.ui.feature.messages.list.adapter.viewholder.decorator.internal

import androidx.core.view.isVisible
import io.getstream.chat.android.ui.feature.messages.list.MessageListView
import io.getstream.chat.android.ui.feature.messages.list.adapter.MessageListItem
import io.getstream.chat.android.ui.feature.messages.list.adapter.viewholder.decorator.BaseDecorator
import io.getstream.chat.android.ui.feature.messages.list.adapter.viewholder.decorator.Decorator
import io.getstream.chat.android.ui.feature.messages.list.adapter.viewholder.impl.CustomAttachmentsViewHolder
import io.getstream.chat.android.ui.feature.messages.list.adapter.viewholder.impl.FileAttachmentsViewHolder
import io.getstream.chat.android.ui.feature.messages.list.adapter.viewholder.impl.GiphyAttachmentViewHolder
import io.getstream.chat.android.ui.feature.messages.list.adapter.viewholder.impl.GiphyViewHolder
import io.getstream.chat.android.ui.feature.messages.list.adapter.viewholder.impl.LinkAttachmentsViewHolder
import io.getstream.chat.android.ui.feature.messages.list.adapter.viewholder.impl.MediaAttachmentsViewHolder
import io.getstream.chat.android.ui.feature.messages.list.adapter.viewholder.impl.MessageDeletedViewHolder
import io.getstream.chat.android.ui.feature.messages.list.adapter.viewholder.impl.MessagePlainTextViewHolder
import io.getstream.chat.android.ui.feature.messages.list.adapter.viewholder.impl.PollViewHolder
import io.getstream.chat.android.ui.widgets.avatar.UserAvatarView

internal class AvatarDecorator(
    private val showAvatarPredicate: MessageListView.ShowAvatarPredicate,
) : BaseDecorator() {

    override val type: Decorator.Type = Decorator.Type.BuiltIn.AVATAR

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
        controlVisibility(viewHolder.binding.userAvatarMineView, viewHolder.binding.userAvatarView, data.isMine)
        setupAvatar(getAvatarView(viewHolder.binding.userAvatarMineView, viewHolder.binding.userAvatarView, data.isMine), data)
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
        controlVisibility(viewHolder.binding.userAvatarMineView, viewHolder.binding.userAvatarView, data.isMine)
        setupAvatar(getAvatarView(viewHolder.binding.userAvatarMineView, viewHolder.binding.userAvatarView, data.isMine), data)
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
        controlVisibility(viewHolder.binding.userAvatarMineView, viewHolder.binding.userAvatarView, data.isMine)
        setupAvatar(getAvatarView(viewHolder.binding.userAvatarMineView, viewHolder.binding.userAvatarView, data.isMine), data)
    }

    /**
     * Decorates the avatars of messages containing image and/or video attachments, based on the message owner.
     *
     * @param viewHolder The holder to decorate.
     * @param data The item that holds all the information.
     */
    override fun decorateMediaAttachmentsMessage(
        viewHolder: MediaAttachmentsViewHolder,
        data: MessageListItem.MessageItem,
    ) {
        controlVisibility(viewHolder.binding.userAvatarMineView, viewHolder.binding.userAvatarView, data.isMine)
        setupAvatar(getAvatarView(viewHolder.binding.userAvatarMineView, viewHolder.binding.userAvatarView, data.isMine), data)
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
        controlVisibility(viewHolder.binding.userAvatarMineView, viewHolder.binding.userAvatarView, data.isMine)
        setupAvatar(getAvatarView(viewHolder.binding.userAvatarMineView, viewHolder.binding.userAvatarView, data.isMine), data)
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
        controlVisibility(viewHolder.binding.userAvatarMineView, viewHolder.binding.userAvatarView, data.isMine)
        setupAvatar(getAvatarView(viewHolder.binding.userAvatarMineView, viewHolder.binding.userAvatarView, data.isMine), data)
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
        controlVisibility(viewHolder.binding.userAvatarMineView, viewHolder.binding.userAvatarView, data.isMine)
        setupAvatar(getAvatarView(viewHolder.binding.userAvatarMineView, viewHolder.binding.userAvatarView, data.isMine), data)
    }

    /**
     * Decorates the avatar of the poll message, based on the message owner.
     *
     * @param viewHolder The holder to decorate.
     * @param data The item that holds all the information.
     */
    override fun decoratePollMessage(viewHolder: PollViewHolder, data: MessageListItem.MessageItem) {
        controlVisibility(viewHolder.binding.userAvatarMineView, viewHolder.binding.userAvatarView, data.isMine)
        setupAvatar(getAvatarView(viewHolder.binding.userAvatarMineView, viewHolder.binding.userAvatarView, data.isMine), data)
    }

    private fun setupAvatar(avatarView: UserAvatarView, data: MessageListItem.MessageItem) {
        val shouldShow = showAvatarPredicate.shouldShow(data)

        avatarView.isVisible = shouldShow

        if (shouldShow) {
            avatarView.setUser(data.message.user)
        }
    }

    private fun getAvatarView(myAvatar: UserAvatarView, theirAvatar: UserAvatarView, isMine: Boolean): UserAvatarView = if (isMine) myAvatar else theirAvatar

    private fun controlVisibility(myAvatar: UserAvatarView, theirAvatar: UserAvatarView, isMine: Boolean) {
        theirAvatar.isVisible = !isMine
        myAvatar.isVisible = isMine
    }
}
