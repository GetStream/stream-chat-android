// ktlint-disable filename

package io.getstream.chat.docs.kotlin.ui.messages

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.viewModels
import com.google.android.material.datepicker.MaterialDatePicker
import io.getstream.chat.android.ui.common.state.messages.Edit
import io.getstream.chat.android.ui.common.state.messages.MessageMode
import io.getstream.chat.android.ui.common.state.messages.Reply
import io.getstream.chat.android.ui.common.state.messages.composer.MessageComposerState
import io.getstream.chat.android.ui.feature.messages.composer.MessageComposerContext
import io.getstream.chat.android.ui.feature.messages.composer.MessageComposerView
import io.getstream.chat.android.ui.feature.messages.composer.content.DefaultMessageComposerCenterContent
import io.getstream.chat.android.ui.feature.messages.composer.content.DefaultMessageComposerCommandSuggestionsContent
import io.getstream.chat.android.ui.feature.messages.composer.content.DefaultMessageComposerFooterContent
import io.getstream.chat.android.ui.feature.messages.composer.content.DefaultMessageComposerHeaderContent
import io.getstream.chat.android.ui.feature.messages.composer.content.DefaultMessageComposerLeadingContent
import io.getstream.chat.android.ui.feature.messages.composer.content.DefaultMessageComposerMentionSuggestionsContent
import io.getstream.chat.android.ui.feature.messages.composer.content.DefaultMessageComposerTrailingContent
import io.getstream.chat.android.ui.feature.messages.composer.content.MessageComposerContent
import io.getstream.chat.android.ui.feature.messages.list.MessageListView
import io.getstream.chat.android.ui.helper.StyleTransformer
import io.getstream.chat.android.ui.helper.TransformStyle
import io.getstream.chat.android.ui.viewmodel.messages.MessageComposerViewModel
import io.getstream.chat.android.ui.viewmodel.messages.MessageListViewModel
import io.getstream.chat.android.ui.viewmodel.messages.MessageListViewModelFactory
import io.getstream.chat.android.ui.viewmodel.messages.bindView
import io.getstream.chat.docs.R
import io.getstream.chat.docs.databinding.MessageComposerLeadingContentBinding

/**
 * [Message Composer](https://getstream.io/chat/docs/sdk/android/ui/message-components/message-composer)
 */
private object MessageComposer : Fragment() {

    /**
     * [Usage](https://getstream.io/chat/docs/sdk/android/ui/message-components/message-composer/#usage)
     */
    class Usage {

        lateinit var messageComposerView: MessageComposerView
        lateinit var messageListView: MessageListView

        fun usage1() {
            // Create MessageComposerViewModel for a given channel
            val factory = MessageListViewModelFactory(cid = "messaging:123")
            val messageComposerViewModel: MessageComposerViewModel by viewModels { factory }

            // Bind MessageComposerViewModel with MessageComposerView
            messageComposerViewModel.bindView(messageComposerView, viewLifecycleOwner)
        }

        fun usage2() {
            // Create ViewModels for MessageComposerView and MessageListView
            val factory = MessageListViewModelFactory(cid = "messaging:123")
            val messageComposerViewModel: MessageComposerViewModel by viewModels { factory }
            val messageListViewModel: MessageListViewModel by viewModels { factory }

            // Bind MessageComposerViewModel with MessageComposerView
            messageComposerViewModel.bindView(messageComposerView, viewLifecycleOwner)

            // Bind MessageListViewModel with MessageListView
            messageListViewModel.bindView(messageListView, viewLifecycleOwner)

            // Integrate MessageComposerView with MessageListView
            messageListViewModel.mode.observe(viewLifecycleOwner) { mode ->
                when (mode) {
                    is MessageMode.MessageThread -> {
                        messageComposerViewModel.setMessageMode(MessageMode.MessageThread(mode.parentMessage))
                    }
                    is MessageMode.Normal -> {
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

    /**
     * [Handling Actions](https://getstream.io/chat/docs/sdk/android/ui/message-components/message-composer/#handling-actions)
     */
    class HandlingActions {

        private lateinit var messageComposerView: MessageComposerView
        private lateinit var messageComposerViewModel: MessageComposerViewModel

        fun handlingActions1() {
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

        fun handlingActions2() {
            messageComposerView.sendMessageButtonClickListener = {
                messageComposerViewModel.sendMessage()
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
    class Customization {

        private lateinit var context: Context
        private lateinit var supportFragmentManager: FragmentManager
        private lateinit var messageComposerView: MessageComposerView
        private lateinit var messageComposerViewModel: MessageComposerViewModel

        fun styleTransformation() {
            TransformStyle.messageComposerStyleTransformer = StyleTransformer { viewStyle ->
                viewStyle.copy(
                    messageInputTextStyle = viewStyle.messageInputTextStyle.copy(
                        color = ContextCompat.getColor(context, R.color.stream_ui_accent_red)
                    )
                )
            }
        }

        fun contentCustomization1() {
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

        fun contentCustomization2() {
            messageComposerView.setLeadingContent(
                DefaultMessageComposerLeadingContent(context).also {
                    it.attachmentsButtonClickListener = {
                        // Show attachment dialog and invoke messageComposerViewModel.addSelectedAttachments(attachments)
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
                    it.sendMessageButtonClickListener = { messageComposerViewModel.sendMessage() }
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

        fun contentCustomization3() {
            val leadingContent = CustomMessageComposerLeadingContent(context).also {
                it.datePickerButtonClickListener = {
                    // Create an instance of a date picker dialog
                    val datePickerDialog = MaterialDatePicker.Builder
                        .datePicker()
                        .build()

                    datePickerDialog.addOnPositiveButtonClickListener {
                        // Handle date selection
                    }

                    // Show the date picker dialog
                    datePickerDialog.show(supportFragmentManager, null)
                }
            }

            messageComposerView.setLeadingContent(leadingContent)
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
}
