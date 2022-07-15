// ktlint-disable filename

package io.getstream.chat.docs.kotlin.ui.guides

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import com.google.android.material.datepicker.MaterialDatePicker
import io.getstream.chat.android.client.models.Attachment
import io.getstream.chat.android.client.models.ChannelCapabilities
import io.getstream.chat.android.client.models.Message
import io.getstream.chat.android.common.composer.MessageComposerState
import io.getstream.chat.android.common.state.Edit
import io.getstream.chat.android.core.ExperimentalStreamChatApi
import io.getstream.chat.android.ui.ChatUI
import io.getstream.chat.android.ui.message.composer.MessageComposerContext
import io.getstream.chat.android.ui.message.composer.MessageComposerView
import io.getstream.chat.android.ui.message.composer.MessageComposerViewStyle
import io.getstream.chat.android.ui.message.composer.attachment.AttachmentPreviewFactoryManager
import io.getstream.chat.android.ui.message.composer.attachment.AttachmentPreviewViewHolder
import io.getstream.chat.android.ui.message.composer.attachment.factory.AttachmentPreviewFactory
import io.getstream.chat.android.ui.message.composer.attachment.factory.FileAttachmentPreviewFactory
import io.getstream.chat.android.ui.message.composer.attachment.factory.ImageAttachmentPreviewFactory
import io.getstream.chat.android.ui.message.composer.content.MessageComposerContent
import io.getstream.chat.android.ui.message.composer.viewmodel.MessageComposerViewModel
import io.getstream.chat.android.ui.message.list.adapter.MessageListListenerContainer
import io.getstream.chat.android.ui.message.list.adapter.viewholder.attachment.AttachmentFactory
import io.getstream.chat.android.ui.message.list.adapter.viewholder.attachment.AttachmentFactoryManager
import io.getstream.chat.android.ui.message.list.adapter.viewholder.attachment.DefaultQuotedAttachmentMessageFactory
import io.getstream.chat.android.ui.message.list.adapter.viewholder.attachment.InnerAttachmentViewHolder
import io.getstream.chat.android.ui.message.list.adapter.viewholder.attachment.QuotedAttachmentFactory
import io.getstream.chat.android.ui.message.list.adapter.viewholder.attachment.QuotedAttachmentFactoryManager
import io.getstream.chat.docs.databinding.CustomMessageComposerLeadingContentBinding
import io.getstream.chat.docs.databinding.ItemDateAttachmentBinding
import io.getstream.chat.docs.databinding.ItemDateAttachmentPreviewBinding
import io.getstream.chat.docs.databinding.ViewQuotedDateAttachmentBinding
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

/**
 * [Adding Custom Attachments (MessageComposerView)](https://getstream.io/chat/docs/sdk/android/ui/guides/adding-custom-attachments-message-composer/)
 */
@OptIn(ExperimentalStreamChatApi::class)
class AddingCustomAttachmentsSnippet2 : Fragment() {

    private lateinit var messageComposerView: MessageComposerView
    private lateinit var messageComposerViewModel: MessageComposerViewModel

    /**
     * [Sending Date Attachments](https://getstream.io/chat/docs/sdk/android/ui/guides/adding-custom-attachments-message-composer/#sending-date-attachments)
     */
    fun sendingDateAttachments(context: Context) {
        // Set custom leading content view
        messageComposerView.setLeadingContent(
            CustomMessageComposerLeadingContent(context).also {
                it.attachmentsButtonClickListener = { messageComposerView.attachmentsButtonClickListener() }
                it.commandsButtonClickListener = { messageComposerView.commandsButtonClickListener() }
                it.calendarButtonClickListener = {
                    // Create an instance of a date picker dialog
                    val datePickerDialog = MaterialDatePicker.Builder
                        .datePicker()
                        .build()

                    // Add an attachment to the message input when the user selects a date
                    datePickerDialog.addOnPositiveButtonClickListener {
                        val date = DateFormat
                            .getDateInstance(DateFormat.LONG)
                            .format(Date(it))
                        val attachment = Attachment(
                            type = "date",
                            extraData = mutableMapOf("payload" to date)
                        )
                        messageComposerViewModel.addSelectedAttachments(listOf(attachment))
                    }

                    // Show the date picker dialog on a click on the calendar button
                    datePickerDialog.show(childFragmentManager, null)
                }
            }
        )
    }

    private class CustomMessageComposerLeadingContent : FrameLayout, MessageComposerContent {

        private lateinit var binding: CustomMessageComposerLeadingContentBinding
        private lateinit var style: MessageComposerViewStyle

        var attachmentsButtonClickListener: () -> Unit = {}
        var commandsButtonClickListener: () -> Unit = {}

        // Click listener for the date picker button
        var calendarButtonClickListener: () -> Unit = {}

        constructor(context: Context) : this(context, null)

        constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)

        constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
            context,
            attrs,
            defStyleAttr
        ) {
            binding = CustomMessageComposerLeadingContentBinding.inflate(LayoutInflater.from(context), this)
            binding.attachmentsButton.setOnClickListener { attachmentsButtonClickListener() }
            binding.commandsButton.setOnClickListener { commandsButtonClickListener() }

            // Set click listener for the date picker button
            binding.calendarButton.setOnClickListener { calendarButtonClickListener() }
        }

        override fun attachContext(messageComposerContext: MessageComposerContext) {
            this.style = messageComposerContext.style
        }

        override fun renderState(state: MessageComposerState) {
            val canSendMessage = state.ownCapabilities.contains(ChannelCapabilities.SEND_MESSAGE)
            val canUploadFile = state.ownCapabilities.contains(ChannelCapabilities.UPLOAD_FILE)
            val hasTextInput = state.inputValue.isNotEmpty()
            val hasAttachments = state.attachments.isNotEmpty()
            val hasCommandInput = state.inputValue.startsWith("/")
            val hasCommandSuggestions = state.commandSuggestions.isNotEmpty()
            val hasMentionSuggestions = state.mentionSuggestions.isNotEmpty()
            val isInEditMode = state.action is Edit

            binding.attachmentsButton.isEnabled = !hasCommandInput && !hasCommandSuggestions && !hasMentionSuggestions
            binding.attachmentsButton.isVisible =
                style.attachmentsButtonVisible && canSendMessage && canUploadFile && !isInEditMode

            binding.commandsButton.isEnabled = !hasTextInput && !hasAttachments
            binding.commandsButton.isVisible = style.commandsButtonVisible && canSendMessage && !isInEditMode
            binding.commandsButton.isSelected = hasCommandSuggestions
        }
    }

    fun renderingDateAttachmentPreviews() {
        ChatUI.attachmentPreviewFactoryManager = AttachmentPreviewFactoryManager(
            listOf(
                DateAttachmentPreviewFactory(),
                ImageAttachmentPreviewFactory(),
                FileAttachmentPreviewFactory()
            )
        )
    }

    class DateAttachmentPreviewFactory : AttachmentPreviewFactory {

        override fun canHandle(attachment: Attachment): Boolean {
            return attachment.type == "date"
        }

        override fun onCreateViewHolder(
            parentView: ViewGroup,
            attachmentRemovalListener: (Attachment) -> Unit,
        ): AttachmentPreviewViewHolder {
            return ItemDateAttachmentPreviewBinding
                .inflate(LayoutInflater.from(parentView.context), parentView, false)
                .let { DateAttachmentPreviewViewHolder(it, attachmentRemovalListener) }
        }

        class DateAttachmentPreviewViewHolder(
            private val binding: ItemDateAttachmentPreviewBinding,
            private val attachmentRemovalListener: (Attachment) -> Unit,
        ) : AttachmentPreviewViewHolder(binding.root) {

            private lateinit var attachment: Attachment

            init {
                binding.deleteButton.setOnClickListener {
                    attachmentRemovalListener(attachment)
                }
            }

            override fun bind(attachment: Attachment) {
                this.attachment = attachment

                binding.dateTextView.text = attachment.extraData["payload"].toString()
            }
        }
    }

    /**
     * [Rendering Date Attachments](https://getstream.io/chat/docs/sdk/android/ui/guides/adding-custom-attachments-message-composer/#rendering-date-attachments)
     */
    fun renderingDateAttachments() {
        ChatUI.attachmentFactoryManager = AttachmentFactoryManager(listOf(DateAttachmentFactory()))
    }

    class DateAttachmentFactory : AttachmentFactory {

        override fun canHandle(message: Message): Boolean {
            return message.attachments.any { it.type == "date" }
        }

        override fun createViewHolder(
            message: Message,
            listeners: MessageListListenerContainer?,
            parent: ViewGroup,
        ): InnerAttachmentViewHolder {
            return ItemDateAttachmentBinding
                .inflate(LayoutInflater.from(parent.context), parent, false)
                .let { DateAttachmentViewHolder(it, listeners) }
        }

        class DateAttachmentViewHolder(
            private val binding: ItemDateAttachmentBinding,
            listeners: MessageListListenerContainer?,
        ) : InnerAttachmentViewHolder(binding.root) {

            private lateinit var message: Message

            init {
                binding.dateTextView.setOnClickListener {
                    listeners?.attachmentClickListener?.onAttachmentClick(
                        message,
                        message.attachments.first()
                    )
                }
                binding.dateTextView.setOnLongClickListener {
                    listeners?.messageLongClickListener?.onMessageLongClick(message)
                    true
                }
            }

            override fun onBindViewHolder(message: Message) {
                this.message = message

                binding.dateTextView.text = message.attachments
                    .first { it.type == "date" }
                    .extraData["payload"]
                    .toString()
            }
        }
    }

    /**
     * [Rendering Quoted Date Attachments](https://getstream.io/chat/docs/sdk/android/ui/guides/adding-custom-attachments-message-composer/#rendering-quoted-date-attachments)
     */
    fun renderingQuotedDateAttachments() {
        ChatUI.quotedAttachmentFactoryManager = QuotedAttachmentFactoryManager(listOf(
            QuotedDateAttachmentFactory(),
            DefaultQuotedAttachmentMessageFactory()
        ))
    }

    class QuotedDateAttachmentView(context: Context) : FrameLayout(context) {

        private val binding = ViewQuotedDateAttachmentBinding.inflate(LayoutInflater.from(context), this)

        fun showDate(attachment: Attachment) {
            binding.dateTextView.text = parseDate(attachment)
        }

        private fun parseDate(attachment: Attachment): String {
            val date = attachment.extraData["payload"].toString()
            return StringBuilder().apply {
                val dateTime = SimpleDateFormat("MMMMM dd, yyyy", Locale.getDefault()).parse(date) ?: return@apply
                val year = Calendar.getInstance().apply {
                    timeInMillis = dateTime.time
                }.get(Calendar.YEAR)
                if (Calendar.getInstance().get(Calendar.YEAR) != year) {
                    append(year).append("\n")
                }
                append(date.replace(", $year", ""))
            }.toString()
        }
    }

    class QuotedDateAttachmentFactory : QuotedAttachmentFactory {
        override fun canHandle(message: Message): Boolean {
            return message.attachments.any { it.type == "date" }
        }

        override fun generateQuotedAttachmentView(message: Message, parent: ViewGroup): View {
            return QuotedDateAttachmentView(parent.context).apply {
                showDate(message.attachments.first())
            }
        }
    }
}
