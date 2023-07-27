// ktlint-disable filename

package io.getstream.chat.docs.kotlin.ui.messages

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.getstream.sdk.chat.utils.typing.TypingUpdatesBuffer
import com.getstream.sdk.chat.viewmodel.MessageInputViewModel
import io.getstream.chat.android.client.models.Attachment
import io.getstream.chat.android.client.models.Message
import io.getstream.chat.android.client.models.User
import io.getstream.chat.android.ui.StyleTransformer
import io.getstream.chat.android.ui.TransformStyle
import io.getstream.chat.android.ui.message.input.MessageInputView
import io.getstream.chat.android.ui.message.input.transliteration.DefaultStreamTransliterator
import io.getstream.chat.android.ui.message.input.viewmodel.bindView
import io.getstream.chat.android.ui.message.list.viewmodel.factory.MessageListViewModelFactory
import io.getstream.chat.android.ui.suggestion.list.adapter.SuggestionListItem
import io.getstream.chat.android.ui.suggestion.list.adapter.SuggestionListItemViewHolderFactory
import io.getstream.chat.android.ui.suggestion.list.adapter.viewholder.BaseSuggestionItemViewHolder
import io.getstream.chat.docs.R
import io.getstream.chat.docs.databinding.ItemCommandBinding
import java.io.File

/**
 * [Message Input](https://getstream.io/chat/docs/sdk/android/ui/message-components/message-input/)
 */
private class MessageInputViewSnippets : Fragment() {

    private lateinit var messageInputView: MessageInputView

    /**
     * [Usage](https://getstream.io/chat/docs/sdk/android/ui/message-components/message-input/#usage)
     */
    fun usage() {
        // Get ViewModel
        val factory: MessageListViewModelFactory = MessageListViewModelFactory(cid = "channelType:channelId")
        val viewModel: MessageInputViewModel by viewModels { factory }
        // Bind it with MessageInputView
        viewModel.bindView(messageInputView, viewLifecycleOwner)
    }

    /**
     * [Handling Actions](https://getstream.io/chat/docs/sdk/android/ui/message-components/message-input/#handling-actions)
     */
    fun handlingActions() {
        messageInputView.setOnSendButtonClickListener {
            // Handle send button click
        }

        messageInputView.setTypingUpdatesBuffer(object : TypingUpdatesBuffer {
            override fun onKeystroke(inputText: String) {
                // Your custom implementation of TypingUpdatesBuffer
            }

            override fun clear() {
                // Your custom implementation of TypingUpdatesBuffer
            }
        })

        messageInputView.setMaxMessageLengthHandler { messageText, messageLength, maxMessageLength, maxMessageLengthExceeded ->
            if (maxMessageLengthExceeded) {
                // Show custom max-length error
            } else {
                // Hide custom max-length error
            }
        }
        messageInputView.setSendMessageHandler(
            object : MessageInputView.MessageSendHandler {
                override fun sendMessage(messageText: String, messageReplyTo: Message?) {
                    // Handle send message
                }

                override fun sendMessageWithAttachments(
                    message: String,
                    attachmentsWithMimeTypes: List<Pair<File, String?>>,
                    messageReplyTo: Message?,
                ) {
                    // Handle message with attachments
                }

                override fun sendMessageWithCustomAttachments(
                    message: String,
                    attachments: List<Attachment>,
                    messageReplyTo: Message?,
                ) {
                    // Handle message with custom attachments
                }

                override fun sendToThreadWithAttachments(
                    parentMessage: Message,
                    message: String,
                    alsoSendToChannel: Boolean,
                    attachmentsWithMimeTypes: List<Pair<File, String?>>,
                ) {
                    // Handle message to thread with attachments
                }

                override fun sendToThreadWithCustomAttachments(
                    parentMessage: Message,
                    message: String,
                    alsoSendToChannel: Boolean,
                    attachmentsWithMimeTypes: List<Attachment>,
                ) {
                    // Handle message to thread with custom attachments
                }

                override fun sendToThread(parentMessage: Message, messageText: String, alsoSendToChannel: Boolean) {
                    // Handle message to thread
                }

                override fun editMessage(oldMessage: Message, newMessageText: String) {
                    // Handle edit message
                }

                override fun dismissReply() {
                    // Handle dismiss reply
                }
            }
        )
    }

    /**
     * [Customization](https://getstream.io/chat/docs/sdk/android/ui/message-components/message-input/#customization)
     */
    fun customization() {
        TransformStyle.messageInputStyleTransformer = StyleTransformer { viewStyle ->
            viewStyle.copy(
                messageInputTextStyle = viewStyle.messageInputTextStyle.copy(
                    color = ContextCompat.getColor(
                        requireContext(),
                        R.color.stream_ui_white,
                    ),
                )
            )
        }
    }

    fun changingSendMessageButton() {
        messageInputView.setMessageInputModeListener {
            when(it) {
                is MessageInputView.InputMode.Edit -> {
                    messageInputView.setSendMessageButtonEnabledDrawable(requireContext().getDrawable(R.drawable.stream_ui_ic_check_single)!!)
                    messageInputView.setSendMessageButtonDisabledDrawable(requireContext().getDrawable(R.drawable.stream_ui_ic_close)!!)
                }
                else -> {
                    messageInputView.setSendMessageButtonEnabledDrawable(requireContext().getDrawable(R.drawable.stream_ui_ic_filled_up_arrow)!!)
                    messageInputView.setSendMessageButtonDisabledDrawable(requireContext().getDrawable(R.drawable.stream_ui_ic_filled_right_arrow)!!)
                }
            }
        }
    }

    fun customSuggestionItems() {
        class CustomCommandViewHolder(
            private val binding: ItemCommandBinding,
        ) : BaseSuggestionItemViewHolder<SuggestionListItem.CommandItem>(binding.root) {

            override fun bindItem(item: SuggestionListItem.CommandItem) {
                binding.commandNameTextView.text = item.command.name
            }
        }

        class CustomSuggestionListViewHolderFactory : SuggestionListItemViewHolderFactory() {
            override fun createCommandViewHolder(
                parent: ViewGroup,
            ): BaseSuggestionItemViewHolder<SuggestionListItem.CommandItem> {
                return ItemCommandBinding
                    .inflate(LayoutInflater.from(parent.context), parent, false)
                    .let(::CustomCommandViewHolder)
            }
        }

        messageInputView.setSuggestionListViewHolderFactory(CustomSuggestionListViewHolderFactory())
    }

    fun changingMentionSearch(users: List<User>) {
        val defaultUserLookupHandler = MessageInputView.DefaultUserLookupHandler(users)
        messageInputView.setUserLookupHandler(defaultUserLookupHandler)
    }

    fun transliteration(users: List<User>) {
        val defaultUserLookupHandler = MessageInputView.DefaultUserLookupHandler(
            users,
            DefaultStreamTransliterator("Cyrl-Latn")
        )
        messageInputView.setUserLookupHandler(defaultUserLookupHandler)
    }
}
