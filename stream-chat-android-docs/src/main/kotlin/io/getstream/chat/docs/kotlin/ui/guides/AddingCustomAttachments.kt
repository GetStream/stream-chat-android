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
import io.getstream.chat.android.models.Attachment
import io.getstream.chat.android.models.Message
import io.getstream.chat.android.ui.ChatUI
import io.getstream.chat.android.ui.common.feature.messages.composer.capabilities.canSendMessage
import io.getstream.chat.android.ui.common.feature.messages.composer.capabilities.canUploadFile
import io.getstream.chat.android.ui.common.state.messages.Edit
import io.getstream.chat.android.ui.common.state.messages.composer.MessageComposerState
import io.getstream.chat.android.ui.feature.messages.composer.MessageComposerContext
import io.getstream.chat.android.ui.feature.messages.composer.MessageComposerView
import io.getstream.chat.android.ui.feature.messages.composer.MessageComposerViewStyle
import io.getstream.chat.android.ui.feature.messages.composer.attachment.preview.AttachmentPreviewFactoryManager
import io.getstream.chat.android.ui.feature.messages.composer.attachment.preview.AttachmentPreviewViewHolder
import io.getstream.chat.android.ui.feature.messages.composer.attachment.preview.factory.AttachmentPreviewFactory
import io.getstream.chat.android.ui.feature.messages.composer.attachment.preview.factory.FileAttachmentPreviewFactory
import io.getstream.chat.android.ui.feature.messages.composer.attachment.preview.factory.MediaAttachmentPreviewFactory
import io.getstream.chat.android.ui.feature.messages.composer.content.MessageComposerLeadingContent
import io.getstream.chat.android.ui.feature.messages.list.adapter.MessageListListeners
import io.getstream.chat.android.ui.feature.messages.list.adapter.viewholder.attachment.AttachmentFactory
import io.getstream.chat.android.ui.feature.messages.list.adapter.viewholder.attachment.AttachmentFactoryManager
import io.getstream.chat.android.ui.feature.messages.list.adapter.viewholder.attachment.DefaultQuotedAttachmentMessageFactory
import io.getstream.chat.android.ui.feature.messages.list.adapter.viewholder.attachment.InnerAttachmentViewHolder
import io.getstream.chat.android.ui.feature.messages.list.adapter.viewholder.attachment.QuotedAttachmentFactory
import io.getstream.chat.android.ui.feature.messages.list.adapter.viewholder.attachment.QuotedAttachmentFactoryManager
import io.getstream.chat.android.ui.viewmodel.messages.MessageComposerViewModel
import io.getstream.chat.docs.databinding.CustomMessageComposerLeadingContentBinding
import io.getstream.chat.docs.databinding.ItemDateAttachmentBinding
import io.getstream.chat.docs.databinding.ItemDateAttachmentPreviewBinding
import io.getstream.chat.docs.databinding.ViewQuotedDateAttachmentBinding
import java.text.SimpleDateFormat
import java.util.Date

/**
 * [Adding Custom Attachments](https://getstream.io/chat/docs/sdk/android/ui/guides/adding-custom-attachments/)
 */
class AddingCustomAttachments {

    /**
     * [Sending Date Attachments](https://getstream.io/chat/docs/sdk/android/ui/guides/adding-custom-attachments/#sending-date-attachments)
     */
    class SendingDateAttachments : Fragment() {

        private lateinit var messageComposerView: MessageComposerView
        private lateinit var messageComposerViewModel: MessageComposerViewModel

        private class CustomMessageComposerLeadingContent : FrameLayout, MessageComposerLeadingContent {

            private lateinit var binding: CustomMessageComposerLeadingContentBinding
            private lateinit var style: MessageComposerViewStyle

            override var attachmentsButtonClickListener: (() -> Unit)? = {}
            override var commandsButtonClickListener: (() -> Unit)? = {}

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
                binding.attachmentsButton.setOnClickListener { attachmentsButtonClickListener?.invoke() }
                binding.commandsButton.setOnClickListener { commandsButtonClickListener?.invoke() }

                // Set click listener for the date picker button
                binding.calendarButton.setOnClickListener { calendarButtonClickListener() }
            }

            override fun attachContext(messageComposerContext: MessageComposerContext) {
                this.style = messageComposerContext.style
            }

            override fun renderState(state: MessageComposerState) {
                val canSendMessage = state.canSendMessage()
                val canUploadFile = state.canUploadFile()
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

        fun setLeadingContent(context: Context) {
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
                        datePickerDialog.addOnPositiveButtonClickListener { date ->
                            val payload = SimpleDateFormat("MMMM dd, yyyy").format(Date(date))
                            val attachment = Attachment(
                                type = "date",
                                extraData = mutableMapOf("payload" to payload)
                            )
                            messageComposerViewModel.addSelectedAttachments(listOf(attachment))
                        }

                        // Show the date picker dialog on a click on the calendar button
                        datePickerDialog.show(childFragmentManager, null)
                    }
                }
            )
        }

        class DateAttachmentPreviewFactory : AttachmentPreviewFactory {

            override fun canHandle(attachment: Attachment): Boolean {
                return attachment.type == "date"
            }

            override fun onCreateViewHolder(
                parentView: ViewGroup,
                attachmentRemovalListener: (Attachment) -> Unit,
                style: MessageComposerViewStyle?
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

        fun renderingDateAttachmentPreviews() {
            ChatUI.attachmentPreviewFactoryManager = AttachmentPreviewFactoryManager(
                attachmentPreviewFactories = listOf(
                    DateAttachmentPreviewFactory(),
                    // The default factories
                    MediaAttachmentPreviewFactory(),
                    FileAttachmentPreviewFactory()
                )
            )
        }
    }

    /**
     * [Rendering Date Attachments](https://getstream.io/chat/docs/sdk/android/ui/guides/adding-custom-attachments/#rendering-date-attachments)
     */
    class RenderingDateAttachments {

        class DateAttachmentFactory : AttachmentFactory {

            override fun canHandle(message: Message): Boolean {
                return message.attachments.any { it.type == "date" }
            }

            override fun createViewHolder(
                message: Message,
                listeners: MessageListListeners?,
                parent: ViewGroup,
            ): InnerAttachmentViewHolder {
                return ItemDateAttachmentBinding
                    .inflate(LayoutInflater.from(parent.context), parent, false)
                    .let { DateAttachmentViewHolder(it, listeners) }
            }

            class DateAttachmentViewHolder(
                private val binding: ItemDateAttachmentBinding,
                listeners: MessageListListeners?,
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

        fun renderingDateAttachments() {
            ChatUI.attachmentFactoryManager = AttachmentFactoryManager(
                attachmentFactories = listOf(
                    DateAttachmentFactory()
                )
            )
        }
    }

    /**
     * [Rendering Quoted Date Attachments](https://getstream.io/chat/docs/sdk/android/ui/guides/adding-custom-attachments/#rendering-quoted-date-attachments)
     */
    class RenderingQuotedDateAttachments {

        class QuotedDateAttachmentFactory : QuotedAttachmentFactory {
            override fun canHandle(message: Message): Boolean {
                return message.attachments.any { it.type == "date" }
            }

            override fun generateQuotedAttachmentView(message: Message, parent: ViewGroup): View {
                return QuotedDateAttachmentView(parent.context).apply {
                    showDate(message.attachments.first())
                }
            }

            class QuotedDateAttachmentView(context: Context) : FrameLayout(context) {

                private val binding = ViewQuotedDateAttachmentBinding.inflate(LayoutInflater.from(context), this)

                fun showDate(attachment: Attachment) {
                    binding.dateTextView.text = attachment.extraData["payload"]
                        .toString()
                        .replace(",", "\n")
                }
            }
        }

        fun renderingQuotedDateAttachments() {
            ChatUI.quotedAttachmentFactoryManager = QuotedAttachmentFactoryManager(
                quotedAttachmentFactories = listOf(
                    QuotedDateAttachmentFactory(),
                    // The default factory
                    DefaultQuotedAttachmentMessageFactory()
                )
            )
        }
    }
}
