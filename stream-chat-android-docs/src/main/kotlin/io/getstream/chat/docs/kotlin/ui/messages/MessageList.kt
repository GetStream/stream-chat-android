// ktlint-disable filename

package io.getstream.chat.docs.kotlin.ui.messages

import android.graphics.Color
import android.text.format.DateUtils
import android.view.Gravity
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import io.getstream.chat.android.models.Attachment
import io.getstream.chat.android.models.Message
import io.getstream.chat.android.models.Reaction
import io.getstream.chat.android.models.ReactionSorting
import io.getstream.chat.android.models.ReactionSortingByCount
import io.getstream.chat.android.models.User
import io.getstream.chat.android.ui.common.helper.DateFormatter
import io.getstream.chat.android.ui.common.state.messages.list.GiphyAction
import io.getstream.chat.android.ui.feature.messages.list.MessageListView
import io.getstream.chat.android.ui.feature.messages.list.adapter.BaseMessageItemViewHolder
import io.getstream.chat.android.ui.feature.messages.list.adapter.MessageListItem
import io.getstream.chat.android.ui.feature.messages.list.adapter.MessageListItemPayloadDiff
import io.getstream.chat.android.ui.feature.messages.list.adapter.MessageListItemViewHolderFactory
import io.getstream.chat.android.ui.helper.StyleTransformer
import io.getstream.chat.android.ui.helper.TransformStyle
import io.getstream.chat.android.ui.viewmodel.messages.MessageListViewModel
import io.getstream.chat.android.ui.viewmodel.messages.MessageListViewModelFactory
import io.getstream.chat.android.ui.viewmodel.messages.bindView
import io.getstream.chat.docs.R
import io.getstream.chat.docs.databinding.TodayMessageListItemBinding
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.concurrent.TimeUnit

/**
 * [Message List](https://getstream.io/chat/docs/sdk/android/ui/message-components/message-list/)
 */
class MessageListViewSnippets : Fragment() {

    private lateinit var messageListView: MessageListView

    /**
     * [Usage](https://getstream.io/chat/docs/sdk/android/ui/message-components/message-list/#usage)
     */
    fun usage() {
        // Init ViewModel
        val viewModel: MessageListViewModel by viewModels {
            MessageListViewModelFactory(requireContext(), cid = "messaging:123")
        }

        // Bind View and ViewModel
        viewModel.bindView(messageListView, viewLifecycleOwner)
    }

    /**
     * [Handling Actions](https://getstream.io/chat/docs/sdk/android/ui/message-components/message-list/#handling-actions)
     */
    fun handlingActions() {
        messageListView.setLastMessageReadHandler {
            // Handle when last message got read
        }
        messageListView.setEndRegionReachedHandler {
            // Handle when end region reached
        }
        messageListView.setMessageDeleteHandler { message: Message ->
            // Handle when message is going to be deleted
        }
        messageListView.setThreadStartHandler { message: Message ->
            // Handle when new thread for message is started
        }
        messageListView.setMessageFlagHandler { message: Message ->
            // Handle when message is going to be flagged
        }
        messageListView.setMessagePinHandler { message: Message ->
            // Handle when message is going to be pinned
        }
        messageListView.setMessageUnpinHandler { message: Message ->
            // Handle when message is going to be unpinned
        }
        messageListView.setMessageMarkAsUnreadHandler() { message: Message ->
            // Handle when message is going to be marked as unread
        }
        messageListView.setGiphySendHandler { giphyAction: GiphyAction ->
            // Handle when some giphyAction is going to be performed
        }
        messageListView.setMessageRetryHandler { message: Message ->
            // Handle when some failed message is going to be retried
        }
        messageListView.setMessageReactionHandler { message: Message, reactionType: String ->
            // Handle when some reaction for message is going to be send
        }
        messageListView.setMessageReplyHandler { cid: String, message: Message ->
            // Handle when message is going to be replied in the channel with cid
        }
        messageListView.setAttachmentDownloadHandler {
            // Handle when attachment is going to be downloaded
        }
        messageListView.setMessageEditHandler { message ->
            // Handle edit message
        }
    }

    /**
     * [Listeners](https://getstream.io/chat/docs/sdk/android/ui/message-components/message-list/#listeners)
     */
    fun listeners() {
        messageListView.setOnMessageClickListener { message: Message ->
            // Handle message being clicked
            true
        }
        messageListView.setOnEnterThreadListener { message: Message ->
            // Handle thread being entered
            true
        }
        messageListView.setOnAttachmentDownloadClickListener { attachment: Attachment ->
            // Handle clicks on the download attachment button
            true
        }
        messageListView.setOnUserReactionClickListener { message: Message, user: User, reaction: Reaction ->
            // Handle clicks on a reaction left by a user
            true
        }
        messageListView.setOnMessageLongClickListener { message ->
            // Handle message being long clicked
            true
        }
        messageListView.setOnAttachmentClickListener { message, attachment ->
            // Handle attachment being clicked
            true
        }
        messageListView.setOnUserClickListener { user ->
            // Handle user avatar being clicked
            true
        }
    }

    /**
     * [Customization](https://getstream.io/chat/docs/sdk/android/ui/message-components/message-list/#customization)
     */
    fun customization() {
        TransformStyle.messageListItemStyleTransformer = StyleTransformer { defaultViewStyle ->
            defaultViewStyle.copy(
                messageBackgroundColorMine = Color.parseColor("#70AF74"),
                messageBackgroundColorTheirs = Color.WHITE,
                textStyleMine = defaultViewStyle.textStyleMine.copy(color = Color.WHITE),
                textStyleTheirs = defaultViewStyle.textStyleTheirs.copy(color = Color.BLACK),
            )
        }

        TransformStyle.messageListStyleTransformer = StyleTransformer { defaultViewStyle ->
            defaultViewStyle.copy(
                scrollButtonViewStyle = defaultViewStyle.scrollButtonViewStyle.copy(
                    scrollButtonColor = Color.RED,
                    scrollButtonUnreadEnabled = false,
                    scrollButtonIcon = ContextCompat.getDrawable(requireContext(), R.drawable.stream_ui_ic_clock)!!,
                ),
            )
        }

        TransformStyle.messageListItemStyleTransformer = StyleTransformer { defaultViewStyle ->
            defaultViewStyle.copy(
                reactionsViewStyle = defaultViewStyle.reactionsViewStyle.copy(
                    reactionSorting = ReactionSortingByCount,
                ),
            )
        }
    }

    fun channelFeatureFlags() {
        messageListView.setRepliesEnabled(false)
        messageListView.setDeleteMessageEnabled(false)
        messageListView.setEditMessageEnabled(false)
    }

    fun dateFormatter() {
        messageListView.setMessageDateFormatter(
            object : DateFormatter {
                private val dateFormat: DateFormat = SimpleDateFormat("dd/MM/yyyy")
                private val timeFormat: DateFormat = SimpleDateFormat("HH:mm")

                override fun formatDate(date: Date?): String {
                    // Provide a way to format Date
                    return dateFormat.format(date)
                }

                override fun formatTime(date: Date?): String {
                    // Provide a way to format Time
                    return timeFormat.format(date)
                }

                override fun formatRelativeTime(date: Date?): String {
                    // Provide a way to format Relative Time
                    date ?: return ""

                    return DateUtils.getRelativeDateTimeString(
                        context,
                        date.time,
                        DateUtils.MINUTE_IN_MILLIS,
                        DateUtils.WEEK_IN_MILLIS,
                        0,
                    ).toString()
                }

                override fun formatRelativeDate(date: Date): String {
                    // Provide a way to format Relative Date
                    return DateUtils.getRelativeTimeSpanString(
                        date.time,
                        System.currentTimeMillis(),
                        DateUtils.DAY_IN_MILLIS,
                        DateUtils.FORMAT_ABBREV_RELATIVE,
                    ).toString()
                }
            }
        )
    }

    fun customMessagesFilter() {
        val forbiddenWord = "secret"
        val predicate = MessageListView.MessageListItemPredicate { item ->
            !(item is MessageListItem.MessageItem && item.message.text.contains(forbiddenWord))
        }
        messageListView.setMessageListItemPredicate(predicate)
    }

    fun customMessagesView() {
        class TodayViewHolder(
            parentView: ViewGroup,
            private val binding: TodayMessageListItemBinding = TodayMessageListItemBinding.inflate(LayoutInflater.from(
                parentView.context),
                parentView,
                false),
        ) : BaseMessageItemViewHolder<MessageListItem.MessageItem>(binding.root) {

            override fun bindData(data: MessageListItem.MessageItem, diff: MessageListItemPayloadDiff) {
                binding.textLabel.text = data.message.text
            }
        }

        class CustomMessageViewHolderFactory : MessageListItemViewHolderFactory() {
            override fun getItemViewType(item: MessageListItem): Int {
                return if (item is MessageListItem.MessageItem &&
                    item.isTheirs &&
                    item.message.attachments.isEmpty() &&
                    item.message.createdAt.isLessThenDayAgo()
                ) {
                    TODAY_VIEW_HOLDER_TYPE
                } else {
                    super.getItemViewType(item)
                }
            }

            override fun getItemViewType(viewHolder: BaseMessageItemViewHolder<out MessageListItem>): Int {
                if (viewHolder is TodayViewHolder) {
                    return TODAY_VIEW_HOLDER_TYPE
                }
                return super.getItemViewType(viewHolder)
            }

            private fun Date?.isLessThenDayAgo(): Boolean {
                if (this == null) {
                    return false
                }
                val dayInMillis = TimeUnit.DAYS.toMillis(1)
                return time >= System.currentTimeMillis() - dayInMillis
            }

            override fun createViewHolder(
                parentView: ViewGroup,
                viewType: Int,
            ): BaseMessageItemViewHolder<out MessageListItem> {
                return if (viewType == TODAY_VIEW_HOLDER_TYPE) {
                    TodayViewHolder(parentView)
                } else {
                    super.createViewHolder(parentView, viewType)
                }
            }

            private val TODAY_VIEW_HOLDER_TYPE = 1
        }

        fun setCustomViewHolderFactory() {
            messageListView.setMessageViewHolderFactory(CustomMessageViewHolderFactory())
        }
    }

    fun customEmptyState() {
        val textView = TextView(context).apply {
            text = "There are no messages yet"
            setTextColor(Color.RED)
        }
        messageListView.setEmptyStateView(
            view = textView,
            layoutParams = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.WRAP_CONTENT,
                FrameLayout.LayoutParams.WRAP_CONTENT,
                Gravity.CENTER
            )
        )
    }

    fun avatarPredicate() {
        messageListView.setShowAvatarPredicate { messageItem ->
            messageItem.isTheirs
        }
    }
}
