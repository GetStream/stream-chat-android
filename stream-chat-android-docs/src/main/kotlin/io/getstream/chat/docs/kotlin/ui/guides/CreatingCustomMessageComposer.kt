package io.getstream.chat.docs.kotlin.ui.guides

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.channel.ChannelClient
import io.getstream.chat.android.core.internal.InternalStreamChatApi
import io.getstream.chat.android.models.Message
import io.getstream.chat.android.ui.common.state.messages.MessageMode
import io.getstream.chat.android.ui.feature.messages.list.MessageListView
import io.getstream.chat.android.ui.viewmodel.messages.MessageListViewModel
import io.getstream.chat.docs.databinding.ViewCustomMessageComposerBinding

/**
 * [Creating a Custom Message Composer](https://getstream.io/chat/docs/sdk/android/ui/guides/custom-message-composer/)
 */
class CreatingCustomMessageComposer {

    /**
     * [Sending and Editing Messages](https://getstream.io/chat/docs/sdk/android/ui/guides/custom-message-composer/#sending-and-editing-messages)
     */
    class SendingAndEditingMessages {

        private lateinit var customMessageComposerView: CustomMessageComposerView
        private lateinit var messageListView: MessageListView
        private lateinit var cid: String

        @OptIn(InternalStreamChatApi::class)
        class CustomMessageComposerView : ConstraintLayout {

            private val binding = ViewCustomMessageComposerBinding.inflate(LayoutInflater.from(context), this)

            private lateinit var channelClient: ChannelClient

            private var messageToEdit: Message? = null

            constructor(context: Context) : super(context)
            constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
            constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

            init {
                binding.sendButton.setOnClickListener {
                    val text = binding.inputField.text.toString()

                    val messageToEdit = messageToEdit
                    if (messageToEdit != null) {
                        channelClient.updateMessage(messageToEdit.copy(text = text)).enqueue()
                    } else {
                        channelClient.sendMessage(Message(text = text, parentId = null)).enqueue()
                    }

                    this.messageToEdit = null
                    binding.inputField.setText("")
                }
            }

            fun setChannelClient(channelClient: ChannelClient) {
                this.channelClient = channelClient
            }

            fun editMessage(message: Message) {
                this.messageToEdit = message
                binding.inputField.setText(message.text)
            }
        }

        fun sendingAndEditingMessages() {
            customMessageComposerView.setChannelClient(ChatClient.instance().channel(cid))

            messageListView.setMessageEditHandler(customMessageComposerView::editMessage)
        }
    }

    /**
     * [Handling Threads](https://getstream.io/chat/docs/sdk/android/ui/guides/custom-message-composer/#handling-threads)
     */
    class HandlingThreads : Fragment() {

        private lateinit var customMessageComposerView: CustomMessageComposerView
        private lateinit var messageListView: MessageListView
        private lateinit var messageListViewModel: MessageListViewModel
        private lateinit var cid: String

        @OptIn(InternalStreamChatApi::class)
        class CustomMessageComposerView : ConstraintLayout {

            private val binding = ViewCustomMessageComposerBinding.inflate(LayoutInflater.from(context), this)

            private lateinit var channelClient: ChannelClient

            private var messageToEdit: Message? = null
            private var parentMessage: Message? = null

            constructor(context: Context) : super(context)
            constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
            constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

            init {
                binding.sendButton.setOnClickListener {
                    val text = binding.inputField.text.toString()

                    val messageToEdit = messageToEdit
                    if (messageToEdit != null) {
                        channelClient.updateMessage(messageToEdit.copy(text = text)).enqueue()
                    } else {
                        channelClient.sendMessage(Message(text = text, parentId = parentMessage?.id)).enqueue()
                    }

                    this.messageToEdit = null
                    binding.inputField.setText("")
                }
            }

            fun setChannelClient(channelClient: ChannelClient) {
                this.channelClient = channelClient
            }

            fun editMessage(message: Message) {
                this.messageToEdit = message
                binding.inputField.setText(message.text)
            }

            fun setActiveThread(parentMessage: Message) {
                this.parentMessage = parentMessage
                this.messageToEdit = null
                binding.inputField.setText("")
            }

            fun resetThread() {
                this.parentMessage = null
                this.messageToEdit = null
                binding.inputField.setText("")
            }
        }

        fun handlingThreads() {
            customMessageComposerView.setChannelClient(ChatClient.instance().channel(cid))

            messageListView.setMessageEditHandler(customMessageComposerView::editMessage)

            messageListViewModel.mode.observe(viewLifecycleOwner) { mode ->
                when (mode) {
                    is MessageMode.MessageThread -> {
                        customMessageComposerView.setActiveThread(mode.parentMessage)
                    }
                    is MessageMode.Normal -> {
                        customMessageComposerView.resetThread()
                    }
                }
            }
        }
    }
}
