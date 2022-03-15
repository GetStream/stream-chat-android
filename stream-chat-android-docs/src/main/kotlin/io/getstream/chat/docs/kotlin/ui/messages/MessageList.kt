// ktlint-disable filename

package io.getstream.chat.docs.kotlin.ui.messages

import android.graphics.Color
import android.util.Log
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.getstream.sdk.chat.adapter.MessageListItem
import com.getstream.sdk.chat.enums.GiphyAction
import com.getstream.sdk.chat.utils.DateFormatter
import com.getstream.sdk.chat.view.messages.MessageListItemWrapper
import com.getstream.sdk.chat.viewmodel.messages.MessageListViewModel
import io.getstream.chat.android.client.models.Attachment
import io.getstream.chat.android.client.models.Message
import io.getstream.chat.android.client.models.Reaction
import io.getstream.chat.android.client.models.User
import io.getstream.chat.android.ui.StyleTransformer
import io.getstream.chat.android.ui.TransformStyle
import io.getstream.chat.android.ui.message.list.MessageListView
import io.getstream.chat.android.ui.message.list.adapter.BaseMessageItemViewHolder
import io.getstream.chat.android.ui.message.list.adapter.MessageListItemViewHolderFactory
import io.getstream.chat.android.ui.message.list.adapter.MessageListListenerContainer
import io.getstream.chat.android.ui.message.list.adapter.viewholder.attachment.AttachmentFactory
import io.getstream.chat.android.ui.message.list.adapter.viewholder.attachment.AttachmentFactoryManager
import io.getstream.chat.android.ui.message.list.adapter.viewholder.attachment.InnerAttachmentViewHolder
import io.getstream.chat.android.ui.message.list.viewmodel.bindView
import io.getstream.chat.android.ui.message.list.viewmodel.factory.MessageListViewModelFactory
import io.getstream.chat.docs.R
import org.threeten.bp.LocalDateTime
import org.threeten.bp.LocalTime
import org.threeten.bp.format.DateTimeFormatter

/**
 * [Message List](https://getstream.io/chat/docs/sdk/android/ui/components/message-list/)
 */
class MessageListViewSnippets : Fragment() {

    private lateinit var messageListView: MessageListView

    /**
     * [Usage](https://getstream.io/chat/docs/sdk/android/ui/components/message-list/#usage)
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
     * [Handling Actions](https://getstream.io/chat/docs/sdk/android/ui/components/message-list/#handling-actions)
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
        messageListView.setUserMuteHandler { user: User ->
            // Handle when a user is going to be muted
        }
        messageListView.setUserUnmuteHandler { user: User ->
            // Handle when a user is going to be unmuted
        }
        messageListView.setUserBlockHandler { user: User, cid: String ->
            // Handle when a user is going to be blocked in the channel with cid
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
     * [Listeners](https://getstream.io/chat/docs/sdk/android/ui/components/message-list/#listeners)
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
     * [Customization](https://getstream.io/chat/docs/sdk/android/ui/components/message-list/#customization)
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

    fun emptyState() {
        // When there's no results, show empty state
        messageListView.showEmptyStateView()
    }

    fun loadingView() {
        // When loading information, show loading view
        messageListView.showLoadingView()
    }

    fun dateFormatter() {
        messageListView.setMessageDateFormatter(
            object : DateFormatter {
                override fun formatDate(localDateTime: LocalDateTime?): String {
                    // Provide a way to format Date
                    return DateTimeFormatter.ofPattern("dd/MM/yyyy").format(localDateTime)
                }

                override fun formatTime(localTime: LocalTime?): String {
                    // Provide a way to format Time.
                    return DateTimeFormatter.ofPattern("HH:mm").format(localTime)
                }
            }
        )
    }

    fun avatarPredicate() {
        messageListView.setShowAvatarPredicate { messageItem ->
            messageItem.positions.contains(MessageListItem.Position.BOTTOM) && messageItem.isTheirs
        }
    }

    fun customMessagesFilter() {
        messageListView.setMessageListItemPredicate { messageList ->
            // Boolean logic here
            true
        }
    }

    fun setNewMessageBehaviour() {
        messageListView.setNewMessagesBehaviour(MessageListView.NewMessagesBehaviour.COUNT_UPDATE)
    }

    fun setEndRegionReachedHandler(viewModel: MessageListViewModel) {
        messageListView.setEndRegionReachedHandler {
            // Handle pagination and include new logic

            // Option to log the event and use the viewModel
            viewModel.onEvent(MessageListViewModel.Event.EndRegionReached)
            Log.e("LogTag", "On load more")
        }
    }

    fun displayNewMessage() {
        val messageItem = MessageListItem.MessageItem(
            message = Message(text = "Lorem ipsum dolor"),
            positions = listOf(MessageListItem.Position.TOP),
            isMine = true
        )

        val messageItemListWrapper = MessageListItemWrapper(listOf(messageItem))
        messageListView.displayNewMessages(messageItemListWrapper)
    }

    fun attachmentReply() {
        messageListView.setAttachmentReplyOptionClickHandler { resultItem ->
            resultItem.messageId
            // Handle reply to attachment
        }

        messageListView.setAttachmentShowInChatOptionClickHandler { resultItem ->
            resultItem.messageId
            // Handle show in chat
        }

        messageListView.setDownloadOptionHandler { resultItem ->
            resultItem.assetUrl
            // Handle download the attachment
        }

        messageListView.setAttachmentDeleteOptionClickHandler { resultItem ->
            resultItem.assetUrl
            resultItem.imageUrl
            // Handle delete
        }
    }

    class CustomViewHolderFactory {
        private lateinit var messageListView: MessageListView

        private class CustomViewHolderFactory : MessageListItemViewHolderFactory() {
            override fun createViewHolder(
                parentView: ViewGroup,
                viewType: Int,
            ): BaseMessageItemViewHolder<out MessageListItem> {
                // Create a new type of view holder here, if needed
                return super.createViewHolder(parentView, viewType)
            }
        }

        fun setCustomViewHolderFactory() {
            val customViewHolderFactory: MessageListItemViewHolderFactory = CustomViewHolderFactory()
            messageListView.setMessageViewHolderFactory(customViewHolderFactory)
        }
    }

    class CustomAttachmentFactory() {
        private lateinit var messageListView: MessageListView

        private class CustomAttachmentFactory : AttachmentFactory {
            private val MY_URL_ADDRESS = "https://myurl.com"

            override fun canHandle(message: Message): Boolean {
                return message.attachments.any { it.imageUrl?.contains(MY_URL_ADDRESS) == true }
            }

            override fun createViewHolder(
                message: Message,
                listeners: MessageListListenerContainer?,
                parent: ViewGroup,
            ): InnerAttachmentViewHolder {
                // put your custom attachment view creation here
                return CustomInnerAttachmentViewHolder(TextView(parent.context), listeners)
            }
        }

        private class CustomInnerAttachmentViewHolder(
            private val textView: TextView,
            listeners: MessageListListenerContainer?,
        ) : InnerAttachmentViewHolder(textView) {

            private lateinit var message: Message

            init {
                textView.setOnClickListener {
                    listeners?.attachmentClickListener?.onAttachmentClick(message, message.attachments.first())
                }
            }

            override fun onBindViewHolder(message: Message) {
                this.message = message

                textView.text = "Image URL: ${message.attachments.first().imageUrl}"
            }
        }

        fun setAttachmentFactory() {
            val customAttachmentFactory = CustomAttachmentFactory()
            val attachmentFactoryManager = AttachmentFactoryManager(listOf(customAttachmentFactory))
            messageListView.setAttachmentFactoryManager(attachmentFactoryManager)
        }
    }
}
