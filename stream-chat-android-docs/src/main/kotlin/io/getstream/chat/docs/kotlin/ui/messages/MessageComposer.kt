// ktlint-disable filename

package io.getstream.chat.docs.kotlin.ui.messages

import android.content.Context
import android.os.Bundle
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.viewModels
import com.getstream.sdk.chat.viewmodel.messages.MessageListViewModel
import com.google.android.material.datepicker.MaterialDatePicker
import io.getstream.chat.android.client.models.Attachment
import io.getstream.chat.android.common.composer.MessageComposerState
import io.getstream.chat.android.common.state.Edit
import io.getstream.chat.android.common.state.MessageMode
import io.getstream.chat.android.common.state.Reply
import io.getstream.chat.android.ui.StyleTransformer
import io.getstream.chat.android.ui.TransformStyle
import io.getstream.chat.android.ui.message.composer.MessageComposerContext
import io.getstream.chat.android.ui.message.composer.MessageComposerView
import io.getstream.chat.android.ui.message.composer.content.DefaultMessageComposerCenterContent
import io.getstream.chat.android.ui.message.composer.content.DefaultMessageComposerCommandSuggestionsContent
import io.getstream.chat.android.ui.message.composer.content.DefaultMessageComposerFooterContent
import io.getstream.chat.android.ui.message.composer.content.DefaultMessageComposerHeaderContent
import io.getstream.chat.android.ui.message.composer.content.DefaultMessageComposerLeadingContent
import io.getstream.chat.android.ui.message.composer.content.DefaultMessageComposerMentionSuggestionsContent
import io.getstream.chat.android.ui.message.composer.content.DefaultMessageComposerTrailingContent
import io.getstream.chat.android.ui.message.composer.content.MessageComposerContent
import io.getstream.chat.android.ui.message.composer.viewmodel.MessageComposerViewModel
import io.getstream.chat.android.ui.message.composer.viewmodel.bindView
import io.getstream.chat.android.ui.message.list.MessageListView
import io.getstream.chat.android.ui.message.list.viewmodel.factory.MessageListViewModelFactory
import io.getstream.chat.docs.R
import io.getstream.chat.docs.databinding.CustomMessageComposerLeadingContentBinding
import io.getstream.chat.docs.databinding.MessageComposerLeadingContentBinding

/**
 * [Usage](https://getstream.io/chat/docs/sdk/android/ui/message-components/message-composer/#usage)
 */
private object MessageComposerUsageSnippet : Fragment() {

    class ChatFragmentSnippet1 : Fragment() {

        private lateinit var messageComposerView: MessageComposerView

        // Create MessageComposerViewModel for a given channel
        val factory = MessageListViewModelFactory(cid = "channelType:channelId")
        val messageComposerViewModel: MessageComposerViewModel by viewModels { factory }

        override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
            super.onViewCreated(view, savedInstanceState)
            // Bind MessageComposerViewModel with MessageComposerView
            messageComposerViewModel.bindView(messageComposerView, viewLifecycleOwner)
        }
    }

    class ChatFragmentSnippet2 : Fragment() {

        private lateinit var messageComposerView: MessageComposerView
        private lateinit var messageListView: MessageListView

        // Create ViewModels for MessageComposerView and MessageListView
        val factory = MessageListViewModelFactory(cid = "channelType:channelId")
        val messageComposerViewModel: MessageComposerViewModel by viewModels { factory }
        val messageListViewModel: MessageListViewModel by viewModels { factory }

        override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
            super.onViewCreated(view, savedInstanceState)
            // Bind MessageComposerViewModel with MessageComposerView
            messageComposerViewModel.bindView(messageComposerView, viewLifecycleOwner)

            // Integrate MessageComposerView with MessageListView
            messageListViewModel.mode.observe(viewLifecycleOwner) {
                when (it) {
                    is MessageListViewModel.Mode.Thread -> {
                        messageComposerViewModel.setMessageMode(MessageMode.MessageThread(it.parentMessage))
                    }
                    is MessageListViewModel.Mode.Normal -> {
                        messageComposerViewModel.leaveThread()
                    }
                }
            }
            messageListView.setMessageReplyHandler { _, message ->
                messageComposerViewModel.performMessageAction(Reply(message))
            }
            messageListView.setMessageEditHandler { message ->
                messageComposerViewModel.performMessageAction(Edit(message))
            }
        }
    }
}

/**
 * [Handling Actions](https://getstream.io/chat/docs/sdk/android/ui/message-components/message-composer/#handling-actions)
 */
private object MessageComposerHandlingActionsSnippet {

    private lateinit var messageComposerView: MessageComposerView
    private lateinit var messageComposerViewModel: MessageComposerViewModel

    fun handlingActionsSnippet1() {
        messageComposerView.sendMessageButtonClickListener = {
            // Handle send button click
        }
        messageComposerView.textInputChangeListener = { text ->
            // Handle input text change
        }
        messageComposerView.attachmentSelectionListener = { attachments ->
            // Handle attachment selection
        }
        messageComposerView.attachmentRemovalListener = { attachment ->
            // Handle attachment removal
        }
        messageComposerView.mentionSelectionListener = { user ->
            // Handle mention selection
        }
        messageComposerView.commandSelectionListener = { command ->
            // Handle command selection
        }
        messageComposerView.alsoSendToChannelSelectionListener = { checked ->
            // Handle "also send to channel" checkbox selection
        }
        messageComposerView.dismissActionClickListener = {
            // Handle dismiss action button click
        }
        messageComposerView.commandsButtonClickListener = {
            // Handle commands button click
        }
        messageComposerView.dismissSuggestionsListener = {
            // Handle when suggestions popup is dismissed
        }
        messageComposerView.attachmentsButtonClickListener = {
            // Handle attachments button click
        }
    }

    fun handlingActionsSnippet2() {
        messageComposerView.sendMessageButtonClickListener = {
            messageComposerViewModel.sendMessage(messageComposerViewModel.buildNewMessage())
        }
        messageComposerView.textInputChangeListener = { text ->
            messageComposerViewModel.setMessageInput(text)
        }
        messageComposerView.attachmentSelectionListener = { attachments ->
            messageComposerViewModel.addSelectedAttachments(attachments)
        }
        messageComposerView.attachmentRemovalListener = { attachment ->
            messageComposerViewModel.removeSelectedAttachment(attachment)
        }
        messageComposerView.mentionSelectionListener = { user ->
            messageComposerViewModel.selectMention(user)
        }
        messageComposerView.commandSelectionListener = { command ->
            messageComposerViewModel.selectCommand(command)
        }
        messageComposerView.alsoSendToChannelSelectionListener = { checked ->
            messageComposerViewModel.setAlsoSendToChannel(checked)
        }
        messageComposerView.dismissActionClickListener = {
            messageComposerViewModel.dismissMessageActions()
        }
        messageComposerView.commandsButtonClickListener = {
            messageComposerViewModel.toggleCommandsVisibility()
        }
        messageComposerView.dismissSuggestionsListener = {
            messageComposerViewModel.dismissSuggestionsPopup()
        }
        messageComposerView.attachmentsButtonClickListener = {
            // Handle attachments button click
        }
    }
}

/**
 * [Customization](https://getstream.io/chat/docs/sdk/android/ui/message-components/message-composer/#customization)
 */
private object MessageComposerCustomizationSnippet {

    private lateinit var context: Context
    private lateinit var fragmentManager: FragmentManager
    private lateinit var messageComposerView: MessageComposerView
    private lateinit var messageComposerViewModel: MessageComposerViewModel

    fun styleTransformationSnippet() {
        TransformStyle.messageComposerStyleTransformer = StyleTransformer { viewStyle ->
            viewStyle.copy(
                messageInputTextStyle = viewStyle.messageInputTextStyle.copy(
                    color = ContextCompat.getColor(context, R.color.stream_ui_accent_red)
                )
            )
        }
    }

    fun contentCustomizationSnippet1() {
        messageComposerView.setLeadingContent(
            DefaultMessageComposerLeadingContent(context).also {
                it.attachmentsButtonClickListener = { messageComposerView.attachmentsButtonClickListener() }
                it.commandsButtonClickListener = { messageComposerView.commandsButtonClickListener() }
            }
        )
        messageComposerView.setCenterContent(
            DefaultMessageComposerCenterContent(context).also {
                it.textInputChangeListener = { text -> messageComposerView.textInputChangeListener(text) }
                it.attachmentRemovalListener =
                    { attachment -> messageComposerView.attachmentRemovalListener(attachment) }
            }
        )
        messageComposerView.setTrailingContent(
            DefaultMessageComposerTrailingContent(context).also {
                it.sendMessageButtonClickListener = { messageComposerView.sendMessageButtonClickListener() }
            }
        )
        messageComposerView.setHeaderContent(
            DefaultMessageComposerHeaderContent(context).also {
                it.dismissActionClickListener = { messageComposerView.dismissActionClickListener() }
            }
        )
        messageComposerView.setFooterContent(
            DefaultMessageComposerFooterContent(context).also {
                it.alsoSendToChannelSelectionListener =
                    { checked -> messageComposerView.alsoSendToChannelSelectionListener(checked) }
            }
        )
        messageComposerView.setCommandSuggestionsContent(
            DefaultMessageComposerCommandSuggestionsContent(context).also {
                it.commandSelectionListener = { command -> messageComposerView.commandSelectionListener(command) }
            }
        )
        messageComposerView.setMentionSuggestionsContent(
            DefaultMessageComposerMentionSuggestionsContent(context).also {
                it.mentionSelectionListener = { user -> messageComposerView.mentionSelectionListener(user) }
            }
        )
    }

    fun contentCustomizationSnippet2() {
        messageComposerView.setLeadingContent(
            DefaultMessageComposerLeadingContent(context).also {
                it.attachmentsButtonClickListener = {
                    val attachmentSelectionListener = { attachments: List<Attachment> ->
                        messageComposerViewModel.addSelectedAttachments(attachments)
                    }
                    // Show attachment dialog and invoke attachmentSelectionListener
                }
                it.commandsButtonClickListener = { messageComposerViewModel.toggleCommandsVisibility() }
            }
        )
        messageComposerView.setCenterContent(
            DefaultMessageComposerCenterContent(context).also {
                it.textInputChangeListener = { text -> messageComposerViewModel.setMessageInput(text) }
                it.attachmentRemovalListener =
                    { attachment -> messageComposerViewModel.removeSelectedAttachment(attachment) }
            }
        )
        messageComposerView.setTrailingContent(
            DefaultMessageComposerTrailingContent(context).also {
                it.sendMessageButtonClickListener =
                    { messageComposerViewModel.sendMessage(messageComposerViewModel.buildNewMessage()) }
            }
        )
        messageComposerView.setHeaderContent(
            DefaultMessageComposerHeaderContent(context).also {
                it.dismissActionClickListener = { messageComposerViewModel.dismissMessageActions() }
            }
        )
        messageComposerView.setFooterContent(
            DefaultMessageComposerFooterContent(context).also {
                it.alsoSendToChannelSelectionListener =
                    { checked -> messageComposerViewModel.setAlsoSendToChannel(checked) }
            }
        )
        messageComposerView.setCommandSuggestionsContent(
            DefaultMessageComposerCommandSuggestionsContent(context).also {
                it.commandSelectionListener = { command -> messageComposerViewModel.selectCommand(command) }
            }
        )
        messageComposerView.setMentionSuggestionsContent(
            DefaultMessageComposerMentionSuggestionsContent(context).also {
                it.mentionSelectionListener = { user -> messageComposerViewModel.selectMention(user) }
            }
        )
    }

    fun contentCustomizationSnippet3() {
        messageComposerView.setLeadingContent(
            CustomMessageComposerLeadingContent(context).also {
                it.datePickerButtonClickListener = {
                    val datePickerDialog = MaterialDatePicker.Builder
                        .datePicker()
                        .build()

                    datePickerDialog.addOnPositiveButtonClickListener {
                        // Handle date selection
                    }

                    datePickerDialog.show(fragmentManager, null)
                }
            }
        )
    }

    private class CustomMessageComposerLeadingContent : FrameLayout, MessageComposerContent {

        private lateinit var binding: MessageComposerLeadingContentBinding

        public var datePickerButtonClickListener: () -> Unit = {}

        constructor(context: Context) : this(context, null)

        constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)

        constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
            context,
            attrs,
            defStyleAttr
        ) {
            binding = MessageComposerLeadingContentBinding.inflate(LayoutInflater.from(context), this, true)
            binding.datePickerButton.setOnClickListener { datePickerButtonClickListener() }
        }

        override fun attachContext(messageComposerContext: MessageComposerContext) {
            // Access the style if necessary
            val style = messageComposerContext.style
        }

        override fun renderState(state: MessageComposerState) {
            // Render the state of the component
        }
    }
}
