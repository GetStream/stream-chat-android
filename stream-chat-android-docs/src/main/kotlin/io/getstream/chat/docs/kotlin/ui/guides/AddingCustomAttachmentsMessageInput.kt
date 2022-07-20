// ktlint-disable filename

package io.getstream.chat.docs.kotlin.ui.guides

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.fragment.app.Fragment
import com.google.android.material.datepicker.MaterialDatePicker
import io.getstream.chat.android.client.models.Attachment
import io.getstream.chat.android.client.models.Message
import io.getstream.chat.android.core.ExperimentalStreamChatApi
import io.getstream.chat.android.ui.ChatUI
import io.getstream.chat.android.ui.message.input.MessageInputView
import io.getstream.chat.android.ui.message.input.attachment.selected.internal.BaseSelectedCustomAttachmentViewHolder
import io.getstream.chat.android.ui.message.input.attachment.selected.internal.SelectedCustomAttachmentViewHolderFactory
import io.getstream.chat.android.ui.message.list.adapter.MessageListListenerContainer
import io.getstream.chat.android.ui.message.list.adapter.viewholder.attachment.AttachmentFactory
import io.getstream.chat.android.ui.message.list.adapter.viewholder.attachment.AttachmentFactoryManager
import io.getstream.chat.android.ui.message.list.adapter.viewholder.attachment.DefaultQuotedAttachmentMessageFactory
import io.getstream.chat.android.ui.message.list.adapter.viewholder.attachment.InnerAttachmentViewHolder
import io.getstream.chat.android.ui.message.list.adapter.viewholder.attachment.QuotedAttachmentFactory
import io.getstream.chat.android.ui.message.list.adapter.viewholder.attachment.QuotedAttachmentFactoryManager
import io.getstream.chat.docs.databinding.ItemDateAttachmentBinding
import io.getstream.chat.docs.databinding.ItemDateAttachmentPreviewBinding
import io.getstream.chat.docs.databinding.ViewQuotedDateAttachmentBinding
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

/**
 * [Adding Custom Attachments (MessageInputView)](https://getstream.io/chat/docs/sdk/android/ui/guides/adding-custom-attachments-message-input/)
 */
@OptIn(ExperimentalStreamChatApi::class)
class AddingCustomAttachmentsSnippet : Fragment() {

    private lateinit var messageInputView: MessageInputView

    /**
     * [Sending Date Attachments](https://getstream.io/chat/docs/sdk/android/ui/guides/adding-custom-attachments-message-input/#sending-date-attachments)
     */
    fun sendingDateAttachments() {
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
            messageInputView.submitCustomAttachments(
                attachments = listOf(attachment),
                viewHolderFactory = DateAttachmentPreviewFactory()
            )
        }

        // Show the date picker dialog on a click on the attachment button
        messageInputView.setAttachmentButtonClickListener {
            datePickerDialog.show(requireActivity().supportFragmentManager, null)
        }
    }

    class DateAttachmentPreviewFactory : SelectedCustomAttachmentViewHolderFactory {
        override fun createAttachmentViewHolder(
            attachments: List<Attachment>,
            parent: ViewGroup,
        ): BaseSelectedCustomAttachmentViewHolder {
            return ItemDateAttachmentPreviewBinding
                .inflate(LayoutInflater.from(parent.context), parent, false)
                .let(::DateAttachmentPreviewViewHolder)
        }

        class DateAttachmentPreviewViewHolder(
            private val binding: ItemDateAttachmentPreviewBinding,
        ) : BaseSelectedCustomAttachmentViewHolder(binding.root) {

            override fun bind(attachment: Attachment, onAttachmentCancelled: (Attachment) -> Unit) {
                binding.dateTextView.text = attachment.extraData["payload"].toString()
                binding.deleteButton.setOnClickListener {
                    onAttachmentCancelled(attachment)
                }
            }
        }
    }

    /**
     * [Rendering Date Attachments](https://getstream.io/chat/docs/sdk/android/ui/guides/adding-custom-attachments-message-input/#rendering-date-attachments)
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
     * [Rendering Quoted Date Attachments](https://getstream.io/chat/docs/sdk/android/ui/guides/adding-custom-attachments-message-input/#rendering-quoted-date-attachments)
     */
    fun renderingQuotedDateAttachments() {
        ChatUI.quotedAttachmentFactoryManager = QuotedAttachmentFactoryManager(listOf(
            QuotedDateAttachmentFactory(),
            DefaultQuotedAttachmentMessageFactory()
        ))
    }

    class QuotedDateAttachmentView(context: Context): FrameLayout(context) {

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

    class QuotedDateAttachmentFactory: QuotedAttachmentFactory {
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
