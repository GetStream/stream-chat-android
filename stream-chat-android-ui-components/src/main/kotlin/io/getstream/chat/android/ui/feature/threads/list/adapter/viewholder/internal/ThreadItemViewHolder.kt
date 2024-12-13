/*
 * Copyright (c) 2014-2024 Stream.io Inc. All rights reserved.
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

package io.getstream.chat.android.ui.feature.threads.list.adapter.viewholder.internal

import android.text.SpannableStringBuilder
import android.view.ViewGroup
import androidx.core.view.isVisible
import io.getstream.chat.android.models.Message
import io.getstream.chat.android.models.Thread
import io.getstream.chat.android.models.User
import io.getstream.chat.android.ui.ChatUI
import io.getstream.chat.android.ui.R
import io.getstream.chat.android.ui.databinding.StreamUiItemThreadListBinding
import io.getstream.chat.android.ui.feature.threads.list.ThreadListView
import io.getstream.chat.android.ui.feature.threads.list.ThreadListViewStyle
import io.getstream.chat.android.ui.feature.threads.list.adapter.ThreadListItem
import io.getstream.chat.android.ui.feature.threads.list.adapter.viewholder.BaseThreadListItemViewHolder
import io.getstream.chat.android.ui.font.setTextStyle
import io.getstream.chat.android.ui.utils.extensions.bold
import io.getstream.chat.android.ui.utils.extensions.getAttachmentsText
import io.getstream.chat.android.ui.utils.extensions.getTranslatedText
import io.getstream.chat.android.ui.utils.extensions.streamThemeInflater

/**
 * Default ViewHolder for thread items.
 *
 * @param parentView The parent view group.
 * @param style The style object for customizing the thread item appearance.
 * @param clickListener The listener for item clicks.
 * @param binding The view binding for the thread item.
 */
internal class ThreadItemViewHolder(
    parentView: ViewGroup,
    style: ThreadListViewStyle,
    private val clickListener: ThreadListView.ThreadClickListener?,
    private val binding: StreamUiItemThreadListBinding = StreamUiItemThreadListBinding.inflate(
        parentView.streamThemeInflater,
        parentView,
        false,
    ),
) : BaseThreadListItemViewHolder<ThreadListItem.ThreadItem>(binding.root) {

    private lateinit var thread: Thread

    init {
        applyStyle(style)
        binding.root.setOnClickListener {
            clickListener?.onThreadClick(thread)
        }
    }

    override fun bind(item: ThreadListItem.ThreadItem) {
        this.thread = item.thread
        val currentUser = ChatUI.currentUserProvider.getCurrentUser()
        bindThreadTitle(currentUser)
        bindReplyTo()
        bindUnreadCountBadge(currentUser)
        bindLatestReply()
    }

    private fun applyStyle(style: ThreadListViewStyle) {
        binding.threadImage.setImageDrawable(style.threadIconDrawable)
        binding.threadTitleTextView.setTextStyle(style.threadTitleStyle)
        binding.replyToTextView.setTextStyle(style.threadReplyToStyle)
        // Remove ripple from latestReplyMessageView
        binding.latestReplyMessageView.binding.root.background = null
        binding.latestReplyMessageView.styleView(style.latestReplyStyle)
        binding.unreadCountBadge.setTextStyle(style.unreadCountBadgeTextStyle)
        binding.unreadCountBadge.background = style.unreadCountBadgeBackground
    }

    private fun bindThreadTitle(currentUser: User?) {
        val channel = thread.channel
        val title = channel
            ?.let { ChatUI.channelNameFormatter.formatChannelName(channel = it, currentUser = currentUser) }
            ?: thread.title
        binding.threadTitleTextView.text = title
    }

    private fun bindReplyTo() {
        val prefix = binding.root.context.getString(R.string.stream_ui_thread_list_replied_to)
        val parentMessageText = formatMessage(thread.parentMessage)
        val replyToText = "$prefix$parentMessageText"
        binding.replyToTextView.text = replyToText
    }

    private fun bindUnreadCountBadge(currentUser: User?) {
        val unreadCount = thread.read
            .find { it.user.id == currentUser?.id }
            ?.unreadMessages
            ?: 0
        if (unreadCount > 0) {
            binding.unreadCountBadge.text =
                if (unreadCount > MAX_UNREAD_COUNT) "$MAX_UNREAD_COUNT+" else unreadCount.toString()
            binding.unreadCountBadge.isVisible = true
        } else {
            binding.unreadCountBadge.isVisible = false
        }
    }

    private fun bindLatestReply() {
        val latestReply = thread.latestReplies.lastOrNull()
        if (latestReply != null) {
            // User avatar
            binding.latestReplyMessageView.binding.userAvatarView.setUser(latestReply.user)
            // Sender name
            binding.latestReplyMessageView.binding.senderNameLabel.text = latestReply.user.name.bold()
            // Reply text
            binding.latestReplyMessageView.binding.messageLabel.text = formatMessage(latestReply)
            // Timestamp
            binding.latestReplyMessageView.binding.messageTimeLabel.text =
                ChatUI.dateFormatter.formatDate(latestReply.createdAt ?: latestReply.createdLocallyAt)
            binding.latestReplyMessageView.isVisible = true
        } else {
            // Note: should never happen
            binding.latestReplyMessageView.isVisible = false
        }
    }

    private fun formatMessage(message: Message): CharSequence {
        val attachmentsText = message.getAttachmentsText()
        val displayedText = message.getTranslatedText()
        val previewText = displayedText.trim()
        return listOf(previewText, attachmentsText)
            .filterNot { it.isNullOrEmpty() }
            .joinTo(SpannableStringBuilder(), " ")
    }

    private companion object {
        private const val MAX_UNREAD_COUNT = 99
    }
}
