package io.getstream.chat.android.ui.message.input.internal

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import androidx.annotation.ColorInt
import androidx.annotation.Px
import androidx.core.view.isVisible
import androidx.core.widget.doAfterTextChanged
import com.getstream.sdk.chat.model.AttachmentMetaData
import com.getstream.sdk.chat.utils.StorageHelper
import io.getstream.chat.android.client.models.Command
import io.getstream.chat.android.client.models.Message
import io.getstream.chat.android.client.models.User
import io.getstream.chat.android.client.models.name
import io.getstream.chat.android.livedata.ChatDomain
import io.getstream.chat.android.ui.R
import io.getstream.chat.android.ui.common.extensions.internal.EMPTY
import io.getstream.chat.android.ui.common.extensions.internal.leftDrawable
import io.getstream.chat.android.ui.common.extensions.internal.setTextSizePx
import io.getstream.chat.android.ui.databinding.StreamUiMessageInputFieldBinding
import io.getstream.chat.android.ui.message.input.attachment.selected.internal.SelectedFileAttachmentAdapter
import io.getstream.chat.android.ui.message.input.attachment.selected.internal.SelectedMediaAttachmentAdapter
import java.io.File
import kotlin.properties.Delegates

internal class MessageInputFieldView : FrameLayout {
    internal val binding: StreamUiMessageInputFieldBinding =
        StreamUiMessageInputFieldBinding.inflate(LayoutInflater.from(context), this, true)

    private val attachmentModeHint: String = context.getString(R.string.stream_ui_message_input_field_attachment_hint)
    private var normalModeHint: CharSequence? = context.getText(R.string.stream_ui_message_input_field_message_hint)
    private val selectedFileAttachmentAdapter: SelectedFileAttachmentAdapter = SelectedFileAttachmentAdapter()
    private val selectedMediaAttachmentAdapter: SelectedMediaAttachmentAdapter = SelectedMediaAttachmentAdapter()
    private val storageHelper = StorageHelper()

    private var selectedAttachments: List<AttachmentMetaData> = emptyList()
    private var contentChangeListener: ContentChangeListener? = null
    private var maxMessageLength: Int = Integer.MAX_VALUE

    var mode: Mode by Delegates.observable(Mode.MessageMode) { _, oldMode, newMode ->
        if (oldMode != newMode) onModeChanged(newMode)
    }

    var messageText: String
        get() {
            val text = binding.messageEditText.text?.toString() ?: String.EMPTY
            mode.let {
                return when (it) {
                    is Mode.CommandMode -> "/${it.command.name} $text"
                    else -> text
                }
            }
        }
        set(text) {
            binding.messageEditText.apply {
                requestFocus()
                setText(text)
                setSelection(getText()?.length ?: 0)
            }
        }

    private var messageHint: String
        get() {
            return binding.messageEditText.hint.toString()
        }
        set(hint) {
            binding.messageEditText.hint = hint
        }

    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    )

    init {
        with(binding) {
            selectedFileAttachmentsRecyclerView.itemAnimator = null
            selectedFileAttachmentAdapter.onAttachmentCancelled = ::cancelAttachment
            selectedFileAttachmentsRecyclerView.adapter = selectedFileAttachmentAdapter
            selectedMediaAttachmentAdapter.onAttachmentCancelled = ::cancelAttachment
            selectedMediaAttachmentsRecyclerView.adapter = selectedMediaAttachmentAdapter
            messageEditText.doAfterTextChanged {
                onMessageTextChanged()
            }
            clearCommandButton.setOnClickListener {
                resetMode()
            }
            commandBadge.leftDrawable(
                R.drawable.stream_ui_ic_command,
                R.dimen.stream_ui_message_input_command_icon_size
            )
            onModeChanged(mode)
        }
    }

    fun setContentChangeListener(contentChangeListener: ContentChangeListener) {
        this.contentChangeListener = contentChangeListener
    }

    fun setTextColor(@ColorInt color: Int) {
        binding.messageEditText.setTextColor(color)
    }

    fun setHintTextColor(@ColorInt color: Int) {
        binding.messageEditText.setHintTextColor(color)
    }

    fun setTextSizePx(@Px size: Float) {
        binding.messageEditText.setTextSizePx(size)
    }

    fun setInputFieldScrollBarEnabled(enabled: Boolean) {
        binding.messageEditText.isVerticalScrollBarEnabled = enabled
    }

    fun setInputFieldScrollbarFadingEnabled(enabled: Boolean) {
        binding.messageEditText.isVerticalFadingEdgeEnabled = enabled
    }

    fun autoCompleteCommand(command: Command) {
        messageText = "/${command.name} "
        mode = Mode.CommandMode(command)
    }

    fun autoCompleteUser(user: User) {
        messageText = "${messageText.substringBeforeLast("@")}@${user.name} "
    }

    fun getAttachedFiles(): List<File> {
        return selectedAttachments.map {
            storageHelper.getCachedFileFromUri(context, it)
        }
    }

    fun setMaxMessageLength(maxMessageLength: Int) {
        this.maxMessageLength = maxMessageLength
    }

    fun isMaxMessageLengthExceeded(): Boolean {
        return messageText.length > maxMessageLength
    }

    fun onReply(replyMessage: Message) {
        mode = Mode.ReplyMessageMode(replyMessage)
    }

    fun onReplyDismissed() {
        if (mode is Mode.ReplyMessageMode) {
            mode = Mode.MessageMode
        }
    }

    fun onEdit(edit: Message) {
        mode = Mode.EditMessageMode(edit)
    }

    private fun cancelAttachment(attachment: AttachmentMetaData) {
        selectedAttachments = selectedAttachments - attachment
        selectedFileAttachmentAdapter.removeItem(attachment)
        selectedMediaAttachmentAdapter.removeItem(attachment)

        if (selectedAttachments.isEmpty()) {
            clearSelectedAttachments()
        }

        selectedAttachmentsChanged()
    }

    private fun clearSelectedAttachments() {
        selectedAttachments = emptyList()
        binding.selectedFileAttachmentsRecyclerView.isVisible = false
        selectedFileAttachmentAdapter.clear()
        binding.selectedMediaAttachmentsRecyclerView.isVisible = false
        selectedMediaAttachmentAdapter.clear()
    }

    private fun onModeChanged(currentMode: Mode) {
        when (currentMode) {
            is Mode.FileAttachmentMode -> switchToFileAttachmentMode(currentMode)
            is Mode.MediaAttachmentMode -> switchToMediaAttachmentMode(currentMode)
            is Mode.MessageMode -> switchToMessageMode()
            is Mode.EditMessageMode -> switchToEditMode(currentMode)
            is Mode.CommandMode -> switchToCommandMode(currentMode)
            is Mode.ReplyMessageMode -> switchToReplyMessageMode(currentMode)
        }
        contentChangeListener?.onModeChanged(currentMode)
    }

    private fun switchToReplyMessageMode(currentMode: Mode.ReplyMessageMode) {
        switchToMessageMode()
        binding.messageReplyView.setMessage(
            currentMode.repliedMessage,
            ChatDomain.instance().currentUser.id == currentMode.repliedMessage.user.id
        )
        binding.messageReplyView.isVisible = true
    }

    private fun switchToFileAttachmentMode(mode: Mode.FileAttachmentMode) {
        binding.messageEditText.hint = attachmentModeHint

        selectedAttachments = mode.attachments.toList()
        binding.selectedMediaAttachmentsRecyclerView.isVisible = false
        selectedMediaAttachmentAdapter.clear()
        binding.selectedFileAttachmentsRecyclerView.isVisible = true
        selectedFileAttachmentAdapter.setItems(selectedAttachments)

        selectedAttachmentsChanged()
    }

    private fun switchToMediaAttachmentMode(mode: Mode.MediaAttachmentMode) {
        binding.messageEditText.hint = attachmentModeHint

        selectedAttachments = mode.attachments.toList()
        binding.selectedFileAttachmentsRecyclerView.isVisible = false
        selectedFileAttachmentAdapter.clear()
        binding.selectedMediaAttachmentsRecyclerView.isVisible = true
        selectedMediaAttachmentAdapter.setItems(selectedAttachments)

        selectedAttachmentsChanged()
    }

    private fun switchToMessageMode() {
        binding.commandBadge.isVisible = false
        binding.clearCommandButton.isVisible = false
        binding.messageEditText.hint = normalModeHint
        binding.messageReplyView.isVisible = false
    }

    private fun switchToEditMode(mode: Mode.EditMessageMode) {
        binding.messageEditText.hint = normalModeHint

        val oldMessage = mode.oldMessage

        messageText = oldMessage.text
    }

    private fun switchToCommandMode(mode: Mode.CommandMode) {
        messageHint = mode.command.args
        messageText = String.EMPTY

        binding.commandBadge.text = mode.command.name
        binding.commandBadge.isVisible = true
        binding.clearCommandButton.isVisible = true
    }

    fun clearContent() {
        clearSelectedAttachments()
        binding.messageEditText.setText(String.EMPTY)
    }

    private fun hasText(): Boolean = messageText.isNotBlank()

    fun hasAttachments(): Boolean = selectedAttachments.isNotEmpty()

    fun hasContent(): Boolean = hasText() || hasAttachments()

    private fun onMessageTextChanged() {
        configInputEditTextError()
        resetModeIfNecessary()
        contentChangeListener?.onMessageTextChanged(messageText)
    }

    private fun selectedAttachmentsChanged() {
        resetModeIfNecessary()
        contentChangeListener?.onSelectedAttachmentsChanged(selectedAttachments)
    }

    private fun resetModeIfNecessary() {
        if (!hasContent() && (mode is Mode.FileAttachmentMode || mode is Mode.MediaAttachmentMode)) {
            resetMode()
        }
    }

    private fun configInputEditTextError() {
        binding.messageEditText.error = if (isMaxMessageLengthExceeded()) {
            context.getString(R.string.stream_Ui_message_input_field_max_length_error, maxMessageLength)
        } else {
            null
        }
    }

    private fun resetMode() {
        mode = Mode.MessageMode
    }

    interface ContentChangeListener {
        fun onMessageTextChanged(messageText: String)
        fun onSelectedAttachmentsChanged(selectedAttachments: List<AttachmentMetaData>)
        fun onModeChanged(mode: Mode)
    }

    sealed class Mode {
        object MessageMode : Mode()
        data class EditMessageMode(val oldMessage: Message) : Mode()
        data class CommandMode(val command: Command) : Mode()
        data class FileAttachmentMode(val attachments: List<AttachmentMetaData>) : Mode()
        data class MediaAttachmentMode(val attachments: List<AttachmentMetaData>) : Mode()
        data class ReplyMessageMode(val repliedMessage: Message) : Mode()
    }
}
