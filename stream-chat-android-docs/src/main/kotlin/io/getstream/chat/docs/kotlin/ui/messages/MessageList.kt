// ktlint-disable filename

package io.getstream.chat.docs.kotlin.ui.messages

import android.graphics.Color
import android.view.Gravity
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.getstream.sdk.chat.adapter.MessageListItem
import com.getstream.sdk.chat.enums.GiphyAction
import com.getstream.sdk.chat.utils.DateFormatter
import com.getstream.sdk.chat.viewmodel.messages.MessageListViewModel
import io.getstream.chat.android.client.models.Attachment
import io.getstream.chat.android.client.models.Message
import io.getstream.chat.android.client.models.Reaction
import io.getstream.chat.android.client.models.User
import io.getstream.chat.android.ui.StyleTransformer
import io.getstream.chat.android.ui.TransformStyle
import io.getstream.chat.android.ui.message.list.MessageListView
import io.getstream.chat.android.ui.message.list.adapter.BaseMessageItemViewHolder
import io.getstream.chat.android.ui.message.list.adapter.MessageListItemPayloadDiff
import io.getstream.chat.android.ui.message.list.adapter.MessageListItemViewHolderFactory
import io.getstream.chat.android.ui.message.list.viewmodel.bindView
import io.getstream.chat.android.ui.message.list.viewmodel.factory.MessageListViewModelFactory
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
        // Init view model
        val viewModel: MessageListViewModel by viewModels {
            MessageListViewModelFactory(cid = "messaging:123")
        }

        // Bind view and viewModel
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
        messageListView.setGiphySendHandler { message: Message, giphyAction: GiphyAction ->
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
        messageListView.setMessageClickListener { message: Message ->
            // Listen to click on message events
        }
        messageListView.setEnterThreadListener { message: Message ->
            // Listen to events when enter thread associated with a message
        }
        messageListView.setAttachmentDownloadClickListener { attachment: Attachment ->
            // Listen to events when download click for an attachment happens
        }
        messageListView.setUserReactionClickListener { message: Message, user: User, reaction: Reaction ->
            // Listen to clicks on user reactions on the message options overlay
        }
        messageListView.setMessageLongClickListener { message ->
            // Handle long click on message
        }
        messageListView.setAttachmentClickListener { message, attachment ->
            // Handle long click on attachment
        }
        messageListView.setUserClickListener { user ->
            // Handle click on user avatar
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

            override fun bindData(data: MessageListItem.MessageItem, diff: MessageListItemPayloadDiff?) {
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
            messageItem.positions.contains(MessageListItem.Position.BOTTOM) && messageItem.isTheirs
        }
    }
}
