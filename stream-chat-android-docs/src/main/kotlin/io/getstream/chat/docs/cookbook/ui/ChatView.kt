package io.getstream.chat.docs.cookbook.ui

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
import com.getstream.sdk.chat.viewmodel.MessageInputViewModel
import com.getstream.sdk.chat.viewmodel.messages.MessageListViewModel
import io.getstream.chat.android.client.models.name
import io.getstream.chat.android.ui.StyleTransformer
import io.getstream.chat.android.ui.TransformStyle
import io.getstream.chat.android.ui.message.input.MessageInputView
import io.getstream.chat.android.ui.message.input.viewmodel.bindView
import io.getstream.chat.android.ui.message.list.MessageListView
import io.getstream.chat.android.ui.message.list.adapter.BaseMessageItemViewHolder
import io.getstream.chat.android.ui.message.list.adapter.MessageListItemPayloadDiff
import io.getstream.chat.android.ui.message.list.adapter.MessageListItemViewHolderFactory
import io.getstream.chat.android.ui.message.list.adapter.MessageListItemViewType
import io.getstream.chat.android.ui.message.list.adapter.MessageListListenerContainer
import io.getstream.chat.android.ui.message.list.header.MessageListHeaderView
import io.getstream.chat.android.ui.message.list.header.viewmodel.MessageListHeaderViewModel
import io.getstream.chat.android.ui.message.list.header.viewmodel.bindView
import io.getstream.chat.android.ui.message.list.viewmodel.bindView
import io.getstream.chat.android.ui.message.list.viewmodel.factory.MessageListViewModelFactory
import io.getstream.chat.docs.R
import io.getstream.chat.docs.databinding.CustomPlainTextItemBinding

/**
 * @see <a href="https://github.com/GetStream/stream-chat-android/wiki/UI-Cookbook#chat-view">Chat View</a>
 */
class ChatView : Fragment() {

    lateinit var messageListHeaderView: MessageListHeaderView
    lateinit var messageListView: MessageListView
    lateinit var messageInputView: MessageInputView

    fun bindingChatViewModels() {
        // Create view models
        val factory: MessageListViewModelFactory = MessageListViewModelFactory(cid = "channelType:channelId")
        val messageListHeaderViewModel: MessageListHeaderViewModel by viewModels { factory }
        val messageListViewModel: MessageListViewModel by viewModels { factory }
        val messageInputViewModel: MessageInputViewModel by viewModels { factory }

        // Bind view models
        messageListHeaderViewModel.bindView(messageListHeaderView, viewLifecycleOwner)
        messageListViewModel.bindView(messageListView, viewLifecycleOwner)
        messageInputViewModel.bindView(messageInputView, viewLifecycleOwner)

        // Let both message list header and message input know when we open a thread
        messageListViewModel.mode.observe(this) { mode ->
            when (mode) {
                is MessageListViewModel.Mode.Thread -> {
                    messageListHeaderViewModel.setActiveThread(mode.parentMessage)
                    messageInputViewModel.setActiveThread(mode.parentMessage)
                }
                MessageListViewModel.Mode.Normal -> {
                    messageListHeaderViewModel.resetThread()
                    messageInputViewModel.resetThread()
                }
            }
        }

        // Let the message input know when we are editing a message
        messageListView.setMessageEditHandler { message ->
            messageInputViewModel.editMessage.postValue(message)
        }

        // Handle navigate up state
        messageListViewModel.state.observe(this) { state ->
            if (state is MessageListViewModel.State.NavigateUp) {
                // Handle navigate up
            }
        }

        // Handle back button behaviour correctly when you're in a thread
        val backHandler = {
            messageListViewModel.onEvent(MessageListViewModel.Event.BackButtonPressed)
        }
        messageListHeaderView.setBackButtonClickListener(backHandler)
        // You should also consider overriding default Activity's back button behaviour
    }

    fun handlingChatActions() {
        messageListHeaderView.setTitleClickListener {
            // Handle title click
        }
        messageListView.setMessageClickListener { message ->
            // Handle message click
        }
        messageListView.setAttachmentClickListener { message, attachment ->
            // Handle attachment click
        }
        messageListView.setMessageEditHandler { message ->
            // Handle edit message
        }
        messageListView.setMessageDeleteHandler { message ->
            // Handle edit message
        }
        messageInputView.setOnSendButtonClickListener {
            // Handle send button click
        }
    }

    fun chatCustomizations() {
        val textView = TextView(context).apply {
            text = "There are no messages yet"
            setTextColor(Color.GREEN)
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

    fun changingMessagesStyleProgrammatically() {
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
                messageBackgroundColorMine = Color.parseColor("#70AF74"),
                messageBackgroundColorTheirs = Color.WHITE,
                textStyleMine = defaultViewStyle.textStyleMine.copy(color = Color.WHITE),
                textStyleTheirs = defaultViewStyle.textStyleTheirs.copy(color = Color.BLACK),
            )
        }
    }

    fun creatingCustomMessageListViewHolderFactory() {
        messageListView.setMessageViewHolderFactory(CustomMessageListItemViewHolderFactory())
    }
}

class CustomMessageListItemViewHolderFactory : MessageListItemViewHolderFactory() {
    override fun createViewHolder(
        parentView: ViewGroup,
        viewType: Int,
    ): BaseMessageItemViewHolder<out MessageListItem> {
        return if (viewType == MessageListItemViewType.PLAIN_TEXT) {
            CustomMessagePlainTextViewHolder(parentView, listenerContainer)
        } else {
            super.createViewHolder(parentView, viewType)
        }
    }
}

class CustomMessagePlainTextViewHolder(
    parent: ViewGroup,
    listeners: MessageListListenerContainer,
    val binding: CustomPlainTextItemBinding =
        CustomPlainTextItemBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        ),
) : BaseMessageItemViewHolder<MessageListItem.MessageItem>(binding.root) {

    init {
        // Handle clicks
        binding.root.setOnClickListener {
            listeners.messageClickListener.onMessageClick(data.message)
        }
        binding.root.setOnLongClickListener {
            listeners.messageLongClickListener.onMessageLongClick(data.message)
            true
        }
    }

    override fun bindData(data: MessageListItem.MessageItem, diff: MessageListItemPayloadDiff?) {
        binding.messageTextView.text = data.message.text
        binding.signatureTextView.text = "Sent by ${data.message.user.name}"
    }
}
