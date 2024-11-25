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

package io.getstream.chat.android.ui.feature.threads.list.internal

import android.text.SpannableStringBuilder
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import io.getstream.chat.android.models.Message
import io.getstream.chat.android.models.Thread
import io.getstream.chat.android.models.User
import io.getstream.chat.android.ui.ChatUI
import io.getstream.chat.android.ui.R
import io.getstream.chat.android.ui.common.extensions.internal.context
import io.getstream.chat.android.ui.common.extensions.internal.singletonList
import io.getstream.chat.android.ui.databinding.StreamUiItemThreadListBinding
import io.getstream.chat.android.ui.databinding.StreamUiItemThreadListLoadingMoreBinding
import io.getstream.chat.android.ui.feature.threads.list.ThreadListView
import io.getstream.chat.android.ui.feature.threads.list.ThreadListViewStyle
import io.getstream.chat.android.ui.font.setTextStyle
import io.getstream.chat.android.ui.utils.extensions.asMention
import io.getstream.chat.android.ui.utils.extensions.bold
import io.getstream.chat.android.ui.utils.extensions.getAttachmentsText
import io.getstream.chat.android.ui.utils.extensions.getTranslatedText
import io.getstream.chat.android.ui.utils.extensions.streamThemeInflater

/**
 * RecyclerView adapter implementation for displaying a list of threads.
 *
 * @param style The [ThreadListViewStyle] for item customization.
 */
internal class ThreadListAdapter(private val style: ThreadListViewStyle) :
    ListAdapter<ThreadListItem, RecyclerView.ViewHolder>(ThreadListItemDiffCallback) {

    private var clickListener: ThreadListView.ThreadClickListener? = null

    init {
        setHasStableIds(true)
    }

    override fun getItemId(position: Int): Long {
        return getItem(position).stableId
    }

    override fun getItemViewType(position: Int): Int {
        val item = getItem(position)
        return when (item) {
            is ThreadListItem.ThreadItem -> ITEM_THREAD
            is ThreadListItem.LoadingMoreItem -> ITEM_LOADING_MORE
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            ITEM_THREAD -> {
                StreamUiItemThreadListBinding
                    .inflate(parent.streamThemeInflater, parent, false)
                    .let(::ThreadItemViewHolder)
                    .also { it.applyStyle(style) }
            }

            else -> {
                StreamUiItemThreadListLoadingMoreBinding
                    .inflate(parent.streamThemeInflater, parent, false)
                    .let(::LoadingMoreViewHolder)
            }
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item = getItem(position)
        if (item is ThreadListItem.ThreadItem && holder is ThreadItemViewHolder) {
            holder.bind(item.thread)
        }
    }

    /**
     * Sets the listener for clicks on thread items.
     */
    fun setThreadClickListener(clickListener: ThreadListView.ThreadClickListener) {
        this.clickListener = clickListener
    }

    companion object {
        private const val ITEM_THREAD = 0
        private const val ITEM_LOADING_MORE = 1

        private const val MAX_UNREAD_COUNT = 99
    }

    /**
     * ViewHolder for thread items.
     */
    inner class ThreadItemViewHolder(
        private val binding: StreamUiItemThreadListBinding,
    ) : RecyclerView.ViewHolder(binding.root) {

        private lateinit var thread: Thread

        init {
            binding.root.setOnClickListener {
                clickListener?.onThreadClick(thread)
            }
        }

        /**
         * Applies the [ThreadListViewStyle] to the [ThreadItemViewHolder].
         *
         * @param style The style to be applied.
         */
        fun applyStyle(style: ThreadListViewStyle) {
            binding.threadImage.setImageDrawable(style.threadIconDrawable)
            binding.threadTitleTextView.setTextStyle(style.threadTitleStyle)
            binding.replyToTextView.setTextStyle(style.threadReplyToStyle)
            // Remove ripple from latestReplyMessageView
            binding.latestReplyMessageView.binding.root.background = null
            binding.latestReplyMessageView.styleView(style.latestReplyStyle)
            binding.unreadCountBadge.setTextStyle(style.unreadCountBadgeTextStyle)
            binding.unreadCountBadge.background = style.unreadCountBadgeBackground
        }

        /**
         * Binds the given [Thread] to the view.
         */
        fun bind(thread: Thread) {
            this.thread = thread
            val currentUser = ChatUI.currentUserProvider.getCurrentUser()
            bindThreadTitle(thread, currentUser)
            bindReplyTo(thread, currentUser)
            bindUnreadCountBadge(thread, currentUser)
            bindLatestReply(thread, currentUser)
        }

        private fun bindThreadTitle(thread: Thread, currentUser: User?) {
            val channel = thread.channel
            val title = channel
                ?.let { ChatUI.channelNameFormatter.formatChannelName(channel = it, currentUser = currentUser) }
                ?: thread.title
            binding.threadTitleTextView.text = title
        }

        private fun bindReplyTo(thread: Thread, currentUser: User?) {
            val prefix = binding.root.context.getString(R.string.stream_ui_thread_list_replied_to)
            val parentMessageText = formatMessage(thread.parentMessage, currentUser?.asMention(context))
            val replyToText = "$prefix$parentMessageText"
            binding.replyToTextView.text = replyToText
        }

        private fun bindUnreadCountBadge(thread: Thread, currentUser: User?) {
            val unreadCount = thread.read
                .find { it.user.id == currentUser?.id }
                ?.unreadMessages
                ?: 0
            if (unreadCount > 0) {
                binding.unreadCountBadge.text =
                    if (unreadCount > MAX_UNREAD_COUNT) "${MAX_UNREAD_COUNT}+" else unreadCount.toString()
                binding.unreadCountBadge.isVisible = true
            } else {
                binding.unreadCountBadge.isVisible = false
            }
        }

        private fun bindLatestReply(thread: Thread, currentUser: User?) {
            val latestReply = thread.latestReplies.lastOrNull()
            if (latestReply != null) {
                // User avatar
                binding.latestReplyMessageView.binding.userAvatarView.setUser(latestReply.user)
                // Sender name
                binding.latestReplyMessageView.binding.senderNameLabel.text = latestReply.user.name.bold()
                // Reply text
                binding.latestReplyMessageView.binding.messageLabel.text =
                    formatMessage(latestReply, currentUser?.asMention(context))
                // Timestamp
                binding.latestReplyMessageView.binding.messageTimeLabel.text =
                    ChatUI.dateFormatter.formatDate(latestReply.createdAt ?: latestReply.createdLocallyAt)
                binding.latestReplyMessageView.isVisible = true
            } else {
                // Note: should never happen
                binding.latestReplyMessageView.isVisible = false
            }
        }

        private fun formatMessage(message: Message, currentUserMention: String?): CharSequence {
            val attachmentsText = message.getAttachmentsText()
            val displayedText = message.getTranslatedText()
            val previewText = displayedText.trim().let {
                if (currentUserMention != null) {
                    // bold mentions of the current user
                    it.bold(currentUserMention.singletonList(), ignoreCase = true)
                } else {
                    it
                }
            }

            return listOf(previewText, attachmentsText)
                .filterNot { it.isNullOrEmpty() }
                .joinTo(SpannableStringBuilder(), " ")
        }
    }

    /**
     * ViewHolder for the loading more item.
     */
    inner class LoadingMoreViewHolder(
        binding: StreamUiItemThreadListLoadingMoreBinding,
    ) : RecyclerView.ViewHolder(binding.root)

    /**
     * [DiffUtil.ItemCallback] for calculating differences between [ThreadListItem]s.
     */
    private object ThreadListItemDiffCallback : DiffUtil.ItemCallback<ThreadListItem>() {
        override fun areItemsTheSame(oldItem: ThreadListItem, newItem: ThreadListItem): Boolean {
            return oldItem.stableId == newItem.stableId
        }

        override fun areContentsTheSame(oldItem: ThreadListItem, newItem: ThreadListItem): Boolean {
            return if (oldItem is ThreadListItem.ThreadItem && newItem is ThreadListItem.ThreadItem) {
                oldItem.thread == newItem.thread // [Thread] is a data class, equality check is enough
            } else {
                false
            }
        }
    }
}
