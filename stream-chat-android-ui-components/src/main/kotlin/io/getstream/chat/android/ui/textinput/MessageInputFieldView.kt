package io.getstream.chat.android.ui.textinput

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
import io.getstream.chat.android.ui.attachments.selected.SelectedFileAttachmentAdapter
import io.getstream.chat.android.ui.attachments.selected.SelectedMediaAttachmentAdapter
import io.getstream.chat.android.ui.databinding.StreamUiMessageInputFieldBinding
import io.getstream.chat.android.ui.utils.extensions.EMPTY
import io.getstream.chat.android.ui.utils.extensions.leftDrawable
import io.getstream.chat.android.ui.utils.extensions.setTextSizePx
import java.io.File
import kotlin.properties.Delegates

public class MessageInputFieldView : FrameLayout {
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

    public var mode: Mode by Delegates.observable(Mode.MessageMode) { _, oldMode, newMode ->
        if (oldMode != newMode) onModeChanged(newMode)
    }

    public var messageText: String
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

    public constructor(context: Context) : super(context)

    public constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)

    public constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
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

    public fun setContentChangeListener(contentChangeListener: ContentChangeListener) {
        this.contentChangeListener = contentChangeListener
    }

    public fun setTextColor(@ColorInt color: Int) {
        binding.messageEditText.setTextColor(color)
    }

    public fun setHintTextColor(@ColorInt color: Int) {
        binding.messageEditText.setHintTextColor(color)
    }

    public fun setTextSizePx(@Px size: Float) {
        binding.messageEditText.setTextSizePx(size)
    }

    public fun setHint(hint: CharSequence?) {
        normalModeHint = hint
    }

    public fun setInputFieldScrollBarEnabled(enabled: Boolean) {
        binding.messageEditText.isVerticalScrollBarEnabled = enabled
    }

    public fun setInputFieldScrollbarFadingEnabled(enabled: Boolean) {
        binding.messageEditText.isVerticalFadingEdgeEnabled = enabled
    }

    public fun autoCompleteCommand(command: Command) {
        messageText = "/${command.name} "
        mode = Mode.CommandMode(command)
    }

    public fun autoCompleteUser(user: User) {
        messageText = "${messageText.substringBeforeLast("@")}@${user.name} "
    }

    public fun getAttachedFiles(): List<File> {
        return selectedAttachments.map {
            storageHelper.getCachedFileFromUri(context, it)
        }
    }

    public fun setMaxMessageLength(maxMessageLength: Int) {
        this.maxMessageLength = maxMessageLength
    }

    public fun isMaxMessageLengthExceeded(): Boolean {
        return messageText.length > maxMessageLength
    }

    public fun onReply(replyMessage: Message) {
        mode = Mode.ReplyMessageMode(replyMessage)
    }

    public fun onReplyDismissed() {
        if (mode is Mode.ReplyMessageMode) {
            mode = Mode.MessageMode
        }
    }

    public fun onEdit(edit: Message) {
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

    public fun clearContent() {
        clearSelectedAttachments()
        binding.messageEditText.setText(String.EMPTY)
    }

    public fun hasText(): Boolean = messageText.isNotBlank()

    public fun hasAttachments(): Boolean = selectedAttachments.isNotEmpty()

    public fun hasContent(): Boolean = hasText() || hasAttachments()

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

    public interface ContentChangeListener {
        public fun onMessageTextChanged(messageText: String)
        public fun onSelectedAttachmentsChanged(selectedAttachments: List<AttachmentMetaData>)
        public fun onModeChanged(mode: Mode)
    }

    public sealed class Mode {
        public object MessageMode : Mode()
        public data class EditMessageMode(val oldMessage: Message) : Mode()
        public data class CommandMode(val command: Command) : Mode()
        public data class FileAttachmentMode(val attachments: List<AttachmentMetaData>) : Mode()
        public data class MediaAttachmentMode(val attachments: List<AttachmentMetaData>) : Mode()
        public data class ReplyMessageMode(val repliedMessage: Message) : Mode()
    }
}
