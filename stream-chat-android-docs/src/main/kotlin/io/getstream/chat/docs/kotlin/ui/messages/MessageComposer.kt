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
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.datepicker.MaterialDatePicker
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.ChatClient.Companion.instance
import io.getstream.chat.android.client.channel.ChannelClient
import io.getstream.chat.android.models.Filters.eq
import io.getstream.chat.android.models.Member
import io.getstream.chat.android.models.User
import io.getstream.chat.android.models.querysort.QuerySortByField.Companion.descByName
import io.getstream.chat.android.models.querysort.QuerySorter
import io.getstream.chat.android.ui.common.feature.messages.composer.mention.CompatUserLookupHandler
import io.getstream.chat.android.ui.common.feature.messages.composer.mention.DefaultUserLookupHandler
import io.getstream.chat.android.ui.common.feature.messages.composer.mention.DefaultUserQueryFilter
import io.getstream.chat.android.ui.common.feature.messages.composer.mention.UserLookupHandler
import io.getstream.chat.android.ui.common.feature.messages.composer.transliteration.DefaultStreamTransliterator
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
import io.getstream.result.Result
import io.getstream.result.call.Call
import io.getstream.result.call.map
import kotlin.jvm.functions.Function1

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
            val factory = MessageListViewModelFactory(requireContext(), cid = "messaging:123")
            val messageComposerViewModel: MessageComposerViewModel by viewModels { factory }

            // Bind MessageComposerViewModel with MessageComposerView
            messageComposerViewModel.bindView(
                messageComposerView,
                viewLifecycleOwner,
                sendMessageButtonClickListener = {
                    // Handle send button click
                },
                textInputChangeListener = { text ->
                    // Handle input text change
                },
                //.. other listeners
            )
        }

        fun usage2() {
            // Create ViewModels for MessageComposerView and MessageListView
            val factory = MessageListViewModelFactory(requireContext(), cid = "messaging:123")
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
            messageComposerView.audioRecordButtonLockListener = {
                // Handle audio record button lock
            }
            messageComposerView.audioRecordButtonHoldListener = {
                // Handle audio record button hold
            }
            messageComposerView.audioRecordButtonCancelListener = {
                // Handle audio record button cancel
            }
            messageComposerView.audioRecordButtonReleaseListener = {
                // Handle audio record button release
            }
            messageComposerView.audioDeleteButtonClickListener = {
                // Handle audio delete button click
            }
            messageComposerView.audioStopButtonClickListener = {
                // Handle audio stop button click
            }
            messageComposerView.audioPlaybackButtonClickListener = {
                // Handle audio playback button click
            }
            messageComposerView.audioCompleteButtonClickListener = {
                // Handle audio complete button click
            }
            messageComposerView.audioSliderDragStartListener = { progress ->
                // Handle audio slider drag start
            }
            messageComposerView.audioSliderDragStopListener = { progress ->
                // Handle audio slider drag stop
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

    class ChangingMentionSearch(
        private val chatClient: ChatClient,
        private val channelClient: ChannelClient,
        private val messageComposerView: MessageComposerView,
    ) : Fragment() {

        private suspend fun queryMembers(query: String): List<User> {
            val filter = eq("name", query)
            val sort: QuerySorter<Member> = descByName("name")
            val membersCall = channelClient.queryMembers(0, 30, filter, sort, emptyList())
            return membersCall.map { members ->
                members.map { it.user }
            }.await().getOrNull() ?: emptyList()
        }

        fun usage0() {
            val cid = "messaging:123"
            val transliterator = DefaultStreamTransliterator(transliterationId = "Cyrl-Latn")
            DefaultUserLookupHandler(
                chatClient = chatClient,
                channelCid = cid,
                localFilter = DefaultUserQueryFilter(transliterator = transliterator),
            )
        }

        fun usage1() {
            val cid = "messaging:123"
            val defaultUserLookupHandler = DefaultUserLookupHandler(chatClient, cid)

            val factory = MessageListViewModelFactory(
                context = requireContext(), cid = cid, userLookupHandler = defaultUserLookupHandler
            )
            val viewModel: MessageComposerViewModel by viewModels { factory }
            viewModel.bindView(messageComposerView, viewLifecycleOwner)
        }

        fun usage2() {
            val cid = "messaging:123"
            val customUserLookupHandler = UserLookupHandler { query ->
                queryMembers(query)
            }

            val factory = MessageListViewModelFactory(
                context = requireContext(),
                cid = cid,
                userLookupHandler = customUserLookupHandler
            )
            val viewModel: MessageComposerViewModel by viewModels { factory }

            // Bind MessageComposerViewModel with MessageComposerView
            viewModel.bindView(messageComposerView, viewLifecycleOwner)
        }
    }
}
