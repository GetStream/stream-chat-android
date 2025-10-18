/*
 * Copyright (c) 2014-2022 Stream.io Inc. All rights reserved.
 *
 * Licensed under the Stream License;
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    https://github.com/GetStream/stream-chat-android/blob/main/LICENSE
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.getstream.chat.android.guides.catalog.uicomponents.customattachments

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import androidx.activity.addCallback
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import com.google.android.material.datepicker.MaterialDatePicker
import io.getstream.chat.android.guides.databinding.ActivityMessagesBinding
import io.getstream.chat.android.guides.databinding.CustomMessageComposerLeadingContentBinding
import io.getstream.chat.android.models.Attachment
import io.getstream.chat.android.models.ChannelCapabilities
import io.getstream.chat.android.ui.common.feature.messages.composer.capabilities.canSendMessage
import io.getstream.chat.android.ui.common.state.messages.Edit
import io.getstream.chat.android.ui.common.state.messages.MessageMode
import io.getstream.chat.android.ui.common.state.messages.Reply
import io.getstream.chat.android.ui.common.state.messages.composer.MessageComposerState
import io.getstream.chat.android.ui.feature.messages.composer.MessageComposerContext
import io.getstream.chat.android.ui.feature.messages.composer.MessageComposerViewStyle
import io.getstream.chat.android.ui.feature.messages.composer.content.MessageComposerLeadingContent
import io.getstream.chat.android.ui.viewmodel.messages.MessageComposerViewModel
import io.getstream.chat.android.ui.viewmodel.messages.MessageListHeaderViewModel
import io.getstream.chat.android.ui.viewmodel.messages.MessageListViewModel
import io.getstream.chat.android.ui.viewmodel.messages.MessageListViewModelFactory
import io.getstream.chat.android.ui.viewmodel.messages.bindView
import java.text.SimpleDateFormat
import java.util.Date

/**
 * An Activity representing a self-contained chat screen with custom attachment factories.
 */
class MessagesActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMessagesBinding

    private val factory: MessageListViewModelFactory by lazy {
        MessageListViewModelFactory(
            context = this,
            threadLoadOlderToNewer = true,
            cid = requireNotNull(intent.getStringExtra(EXTRA_CID)),
        )
    }
    private val messageListHeaderViewModel: MessageListHeaderViewModel by viewModels { factory }
    private val messageListViewModel: MessageListViewModel by viewModels { factory }
    private val messageComposerViewModel: MessageComposerViewModel by viewModels { factory }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMessagesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        messageListHeaderViewModel.bindView(binding.messageListHeaderView, this)
        messageListViewModel.bindView(binding.messageListView, this)
        messageComposerViewModel.bindView(binding.messageComposerView, this)

        messageListViewModel.mode.observe(this) {
            when (it) {
                is MessageMode.MessageThread -> {
                    messageListHeaderViewModel.setActiveThread(it.parentMessage)
                    messageComposerViewModel.setMessageMode(MessageMode.MessageThread(it.parentMessage))
                }
                is MessageMode.Normal -> {
                    messageListHeaderViewModel.resetThread()
                    messageComposerViewModel.leaveThread()
                }
            }
        }
        binding.messageListView.setMessageReplyHandler { _, message ->
            messageComposerViewModel.performMessageAction(Reply(message))
        }
        binding.messageListView.setMessageEditHandler { message ->
            messageComposerViewModel.performMessageAction(Edit(message))
        }

        messageListViewModel.state.observe(this) { state ->
            if (state is MessageListViewModel.State.NavigateUp) {
                finish()
            }
        }

        val backHandler = {
            messageListViewModel.onEvent(MessageListViewModel.Event.BackButtonPressed)
        }
        binding.messageListHeaderView.setBackButtonClickListener(backHandler)
        onBackPressedDispatcher.addCallback(this) {
            backHandler()
        }

        // Set custom leading content view
        binding.messageComposerView.setLeadingContent(
            CustomMessageComposerLeadingContent(this).also {
                it.attachmentsButtonClickListener = { binding.messageComposerView.attachmentsButtonClickListener() }
                it.commandsButtonClickListener = { binding.messageComposerView.commandsButtonClickListener() }
                it.calendarButtonClickListener = {
                    // Create an instance of a date picker dialog
                    val datePickerDialog = MaterialDatePicker.Builder
                        .datePicker()
                        .build()

                    // Add an attachment to the message input when the user selects a date
                    datePickerDialog.addOnPositiveButtonClickListener { date ->
                        val payload = SimpleDateFormat("MMMM dd, yyyy").format(Date(date))
                        val attachment = Attachment(
                            type = "date",
                            extraData = mutableMapOf("payload" to payload),
                        )
                        messageComposerViewModel.addSelectedAttachments(listOf(attachment))
                    }

                    // Show the date picker dialog at the click of the calendar button
                    datePickerDialog.show(supportFragmentManager, null)
                }
            },
        )
    }

    private class CustomMessageComposerLeadingContent : FrameLayout, MessageComposerLeadingContent {

        private lateinit var binding: CustomMessageComposerLeadingContentBinding
        private lateinit var style: MessageComposerViewStyle

        override var attachmentsButtonClickListener: (() -> Unit)? = null
        override var commandsButtonClickListener: (() -> Unit)? = null

        // Click listener for the date picker button
        var calendarButtonClickListener: () -> Unit = {}

        constructor(context: Context) : this(context, null)

        constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)

        constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
            context,
            attrs,
            defStyleAttr,
        ) {
            binding = CustomMessageComposerLeadingContentBinding.inflate(LayoutInflater.from(context), this)
            binding.attachmentsButton.setOnClickListener { attachmentsButtonClickListener?.invoke() }
            binding.commandsButton.setOnClickListener { commandsButtonClickListener?.invoke() }

            // Set click listener for the date picker button
            binding.calendarButton.setOnClickListener { calendarButtonClickListener() }
        }

        override fun attachContext(messageComposerContext: MessageComposerContext) {
            this.style = messageComposerContext.style
        }

        override fun renderState(state: MessageComposerState) {
            val canSendMessage = canSendMessage(state)
            val canUploadFile = state.ownCapabilities.contains(ChannelCapabilities.UPLOAD_FILE)
            val hasTextInput = state.inputValue.isNotEmpty()
            val hasAttachments = state.attachments.isNotEmpty()
            val hasCommandInput = state.inputValue.startsWith("/")
            val hasCommandSuggestions = state.commandSuggestions.isNotEmpty()
            val hasMentionSuggestions = state.mentionSuggestions.isNotEmpty()
            val isInEditMode = state.action is Edit

            binding.attachmentsButton.isEnabled =
                !hasCommandInput && !hasCommandSuggestions && !hasMentionSuggestions
            binding.attachmentsButton.isVisible =
                style.attachmentsButtonVisible && canSendMessage && canUploadFile && !isInEditMode

            binding.commandsButton.isEnabled = !hasTextInput && !hasAttachments
            binding.commandsButton.isVisible = style.commandsButtonVisible && canSendMessage && !isInEditMode
            binding.commandsButton.isSelected = hasCommandSuggestions
        }
    }

    companion object {
        private const val EXTRA_CID: String = "extra_cid"

        /**
         * Creates an [Intent] to start [MessagesActivity].
         *
         * @param context The context used to create the intent.
         * @param cid The id of the channel.
         * @return The [Intent] to start [MessagesActivity].
         */
        fun createIntent(context: Context, cid: String): Intent {
            return Intent(context, MessagesActivity::class.java).apply {
                putExtra(EXTRA_CID, cid)
            }
        }
    }
}
