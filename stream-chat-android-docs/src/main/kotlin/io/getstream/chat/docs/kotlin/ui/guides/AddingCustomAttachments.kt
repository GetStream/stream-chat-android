// ktlint-disable filename

package io.getstream.chat.docs.kotlin.ui.guides

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.google.android.material.datepicker.MaterialDatePicker
import io.getstream.chat.android.client.models.Attachment
import io.getstream.chat.android.client.models.Message
import io.getstream.chat.android.ui.ChatUI
import io.getstream.chat.android.ui.message.input.MessageInputView
import io.getstream.chat.android.ui.message.input.attachment.selected.internal.BaseSelectedCustomAttachmentViewHolder
import io.getstream.chat.android.ui.message.input.attachment.selected.internal.SelectedCustomAttachmentViewHolderFactory
import io.getstream.chat.android.ui.message.list.adapter.MessageListListenerContainer
import io.getstream.chat.android.ui.message.list.adapter.viewholder.attachment.AttachmentFactory
import io.getstream.chat.android.ui.message.list.adapter.viewholder.attachment.AttachmentFactoryManager
import io.getstream.chat.android.ui.message.list.adapter.viewholder.attachment.InnerAttachmentViewHolder
import io.getstream.chat.docs.databinding.ItemDateAttachmentBinding
import io.getstream.chat.docs.databinding.ItemDateAttachmentPreviewBinding
import java.text.DateFormat
import java.util.Date

/**
 * [Adding Custom Attachments](https://getstream.io/chat/docs/sdk/android/ui/guides/adding-custom-attachments/)
 */
class AddingCustomAttachmentsSnippet : Fragment() {

    private lateinit var messageInputView: MessageInputView

    fun sendingDateAttachments() {
        // Build a date picker
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

        // Show the date picker dialog
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
}
