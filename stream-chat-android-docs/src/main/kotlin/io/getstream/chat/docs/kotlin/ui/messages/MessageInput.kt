// ktlint-disable filename

package io.getstream.chat.docs.kotlin.ui.messages

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.getstream.sdk.chat.viewmodel.MessageInputViewModel
import io.getstream.chat.android.client.models.Attachment
import io.getstream.chat.android.client.models.Message
import io.getstream.chat.android.client.models.User
import io.getstream.chat.android.ui.StyleTransformer
import io.getstream.chat.android.ui.TransformStyle
import io.getstream.chat.android.ui.message.input.MessageInputView
import io.getstream.chat.android.ui.message.input.attachment.selected.internal.BaseSelectedCustomAttachmentViewHolder
import io.getstream.chat.android.ui.message.input.attachment.selected.internal.SelectedCustomAttachmentViewHolderFactory
import io.getstream.chat.android.ui.message.input.transliteration.DefaultStreamTransliterator
import io.getstream.chat.android.ui.message.input.viewmodel.bindView
import io.getstream.chat.android.ui.message.list.viewmodel.factory.MessageListViewModelFactory
import io.getstream.chat.android.ui.suggestion.list.adapter.SuggestionListItem
import io.getstream.chat.android.ui.suggestion.list.adapter.SuggestionListItemViewHolderFactory
import io.getstream.chat.android.ui.suggestion.list.adapter.viewholder.BaseSuggestionItemViewHolder
import io.getstream.chat.docs.R
import io.getstream.chat.docs.databinding.CustomAttachmentItemBinding
import java.io.File

/**
 * [Message Input](https://getstream.io/chat/docs/sdk/android/ui/components/message-input/)
 */
private class MessageInputViewSnippets() : Fragment() {

    private lateinit var messageInputView: MessageInputView

    /**
     * [Usage](https://getstream.io/chat/docs/sdk/android/ui/components/message-input/#usage)
     */
    fun usage() {
        // Get ViewModel
        val factory: MessageListViewModelFactory = MessageListViewModelFactory(cid = "channelType:channelId")
        val viewModel: MessageInputViewModel by viewModels { factory }
        // Bind it with MessageInputView
        viewModel.bindView(messageInputView, viewLifecycleOwner)
    }

    /**
     * [Handling Actions](https://getstream.io/chat/docs/sdk/android/ui/components/message-input/#handling-actions)
     */
    fun handlingActions() {
        messageInputView.setOnSendButtonClickListener {
            // Handle send button click
        }
        messageInputView.setTypingListener(
            object : MessageInputView.TypingListener {
                override fun onKeystroke() {
                    // Handle keystroke case
                }

                override fun onStopTyping() {
                    // Handle stop typing case
                }
            }
        )
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
     * [Customization](https://getstream.io/chat/docs/sdk/android/ui/components/message-input/#customization)
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

    fun customSuggestionListviewHolderFactory() {
        val customViewHolderFactory: SuggestionListItemViewHolderFactory = CustomSuggestionListViewHolderFactory()
        messageInputView.setSuggestionListViewHolderFactory(customViewHolderFactory)
    }

    fun transliterationSupport(users: List<User>) {
        val defaultUserLookupHandler = MessageInputView.DefaultUserLookupHandler(
            users,
            DefaultStreamTransliterator("Cyrl-Latn")
        )
        messageInputView.setUserLookupHandler(defaultUserLookupHandler)
    }

    fun customAttachments() {
        val attachments = listOf(Attachment(title = "A"), Attachment(title = "B"))
        messageInputView.submitCustomAttachments(attachments, MyCustomAttachmentFactory())
    }

    class CustomSuggestionListViewHolderFactory : SuggestionListItemViewHolderFactory() {

        override fun createCommandViewHolder(
            parentView: ViewGroup,
        ): BaseSuggestionItemViewHolder<SuggestionListItem.CommandItem> {
            // Create custom command view holder here
            return super.createCommandViewHolder(parentView)
        }

        override fun createMentionViewHolder(
            parentView: ViewGroup,
        ): BaseSuggestionItemViewHolder<SuggestionListItem.MentionItem> {
            // Create custom mention view holder here
            return super.createMentionViewHolder(parentView)
        }
    }

    class MyCustomViewHolder(
        parentView: ViewGroup,
        private val binding: CustomAttachmentItemBinding = CustomAttachmentItemBinding.inflate(
            LayoutInflater.from(parentView.context),
            parentView,
            false
        ),
    ) : BaseSelectedCustomAttachmentViewHolder(binding.root) {
        override fun bind(attachment: Attachment, onAttachmentCancelled: (Attachment) -> Unit) {
            binding.textView.text = attachment.title
            binding.deleteButton.setOnClickListener {
                onAttachmentCancelled(attachment)
            }
        }
    }

    class MyCustomAttachmentFactory : SelectedCustomAttachmentViewHolderFactory {
        override fun createAttachmentViewHolder(
            attachments: List<Attachment>,
            parent: ViewGroup,
        ): MyCustomViewHolder {
            return MyCustomViewHolder(parent)
        }
    }
}
