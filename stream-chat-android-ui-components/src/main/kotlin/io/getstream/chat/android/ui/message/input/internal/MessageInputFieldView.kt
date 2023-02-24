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

package io.getstream.chat.android.ui.message.input.internal

import android.annotation.TargetApi
import android.content.Context
import android.graphics.drawable.Drawable
import android.os.Build
import android.util.AttributeSet
import android.widget.FrameLayout
import android.widget.Toast
import androidx.annotation.ColorInt
import androidx.annotation.Px
import androidx.core.view.isVisible
import androidx.core.widget.doAfterTextChanged
import com.getstream.sdk.chat.model.AttachmentMetaData
import com.getstream.sdk.chat.utils.AttachmentConstants
import com.getstream.sdk.chat.utils.StorageHelper
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.models.Attachment
import io.getstream.chat.android.client.models.Command
import io.getstream.chat.android.client.models.Constants
import io.getstream.chat.android.client.models.Message
import io.getstream.chat.android.client.models.User
import io.getstream.chat.android.core.ExperimentalStreamChatApi
import io.getstream.chat.android.ui.R
import io.getstream.chat.android.ui.common.extensions.internal.EMPTY
import io.getstream.chat.android.ui.common.extensions.internal.createStreamThemeWrapper
import io.getstream.chat.android.ui.common.extensions.internal.setStartDrawableWithSize
import io.getstream.chat.android.ui.common.extensions.internal.setTextSizePx
import io.getstream.chat.android.ui.common.extensions.internal.streamThemeInflater
import io.getstream.chat.android.ui.common.style.TextStyle
import io.getstream.chat.android.ui.common.style.setTextStyle
import io.getstream.chat.android.ui.databinding.StreamUiMessageInputFieldBinding
import io.getstream.chat.android.ui.message.input.attachment.selected.internal.SelectedCustomAttachmentAdapter
import io.getstream.chat.android.ui.message.input.attachment.selected.internal.SelectedCustomAttachmentViewHolderFactory
import io.getstream.chat.android.ui.message.input.attachment.selected.internal.SelectedFileAttachmentAdapter
import io.getstream.chat.android.ui.message.input.attachment.selected.internal.SelectedMediaAttachmentAdapter
import io.getstream.chat.android.ui.message.list.MessageReplyStyle
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.io.File
import kotlin.properties.Delegates

internal class MessageInputFieldView : FrameLayout {
    internal val binding: StreamUiMessageInputFieldBinding =
        StreamUiMessageInputFieldBinding.inflate(streamThemeInflater, this, true)

    private val attachmentModeHint: String = context.getString(R.string.stream_ui_message_input_only_attachments_hint)
    private var normalModeHint: CharSequence? = context.getText(R.string.stream_ui_message_input_hint)
    private val selectedFileAttachmentAdapter: SelectedFileAttachmentAdapter = SelectedFileAttachmentAdapter()
    private val selectedMediaAttachmentAdapter: SelectedMediaAttachmentAdapter = SelectedMediaAttachmentAdapter()
    private val selectedCustomAttachmentsAdapter: SelectedCustomAttachmentAdapter = SelectedCustomAttachmentAdapter()
    private val storageHelper = StorageHelper()

    private var selectedAttachments: List<AttachmentMetaData> = emptyList()
    private var selectedCustomAttachments: List<Attachment> = emptyList()
    private var contentChangeListener: ContentChangeListener? = null
    private var attachmentMaxFileSize: Long = AttachmentConstants.MAX_UPLOAD_FILE_SIZE
    internal var maxAttachmentsCount: Int = AttachmentConstants.MAX_ATTACHMENTS_COUNT

    private val _hasBigAttachment = MutableStateFlow(false)
    internal val hasBigAttachment: StateFlow<Boolean> = _hasBigAttachment

    private val _selectedAttachmentsCount = MutableStateFlow(0)
    internal val selectedAttachmentsCount: StateFlow<Int> = _selectedAttachmentsCount
    internal var messageReplyStyle: MessageReplyStyle? = null

    var mode: Mode by Delegates.observable(Mode.MessageMode) { _, oldMode, newMode ->
        if (oldMode != newMode) {
            onModeChanged(newMode)
        }
    }

    private fun modeChangeIsAllowed(oldMode: Mode, newMode: Mode): Boolean {
        return if (oldMode is Mode.EditMessageMode && newMode is Mode.CommandMode) {
            false.also {
                Toast.makeText(context, "It is not possible to use a command when editing messages", Toast.LENGTH_SHORT)
                    .show()
            }
        } else true
    }

    var messageText: String
        get() {
            val text = binding.messageEditText.text?.toString() ?: String.EMPTY
            return mode.let { messageMode ->
                when (messageMode) {
                    is Mode.CommandMode -> {
                        text.substringAfter("/${messageMode.command.name} ")
                            .let { "/${messageMode.command.name} $it" }
                    }
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

    internal var messageHint: String
        get() {
            return binding.messageEditText.hint.toString()
        }
        set(hint) {
            binding.messageEditText.hint = hint
        }

    constructor(context: Context) : super(context.createStreamThemeWrapper())

    constructor(context: Context, attrs: AttributeSet?) : super(context.createStreamThemeWrapper(), attrs)

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context.createStreamThemeWrapper(),
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
            selectedCustomAttachmentsAdapter.onAttachmentCancelled = ::cancelCustomAttachment
            selectedCustomAttachmentsRecyclerView.adapter = selectedCustomAttachmentsAdapter
            messageEditText.doAfterTextChanged {
                onMessageTextChanged()
            }
            clearCommandButton.setOnClickListener {
                resetMode()
            }
        }
    }

    @TargetApi(Build.VERSION_CODES.Q)
    fun setCustomCursor(cursor: Drawable) {
        binding.messageEditText.textCursorDrawable = cursor
    }

    fun clearMessageInputFocus() {
        binding.messageEditText.clearFocus()
    }

    fun setContentChangeListener(contentChangeListener: ContentChangeListener) {
        this.contentChangeListener = contentChangeListener
    }

    fun setCustomBackgroundDrawable(drawable: Drawable) {
        binding.containerView.background = drawable
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

    /**
     * Sets the max file size of an attachment. Be aware that this doesn't change the limit of the accepted size of attachments
     * in Stream's backend.
     */
    fun setAttachmentMaxFileMb(size: Int) {
        attachmentMaxFileSize = size * Constants.MB_IN_BYTES

        selectedFileAttachmentAdapter.attachmentMaxFileSize = attachmentMaxFileSize
        selectedMediaAttachmentAdapter.attachmentMaxFileSize = attachmentMaxFileSize
    }

    /**
     * Sets the typeface for [EditText] of MessageInputFieldView.
     */
    fun setTextInputTypefaceStyle(typeface: Int) {
        val originalTypeface = binding.messageEditText.typeface

        binding.messageEditText.setTypeface(originalTypeface, typeface)
    }

    fun setCommandInputCancelIcon(drawable: Drawable) {
        binding.clearCommandButton.setImageDrawable(drawable)
    }

    /**
     * Set the badge icon for the command.
     */
    fun setCommandInputBadgeIcon(drawable: Drawable) {
        binding.commandBadge.setStartDrawableWithSize(drawable, R.dimen.stream_ui_message_input_command_icon_size)
    }

    fun setCommandInputBadgeBackgroundDrawable(drawable: Drawable) {
        binding.commandBadge.background = drawable
    }

    fun setCommandInputBadgeTextStyle(testStyle: TextStyle) {
        binding.commandBadge.setTextStyle(testStyle)
    }

    fun setInputType(inputType: Int) {
        binding.messageEditText.inputType = inputType
    }

    /**
     * Changes mode to command.
     */
    fun autoCompleteCommand(command: Command) {
        val newMode = Mode.CommandMode(command)

        if (modeChangeIsAllowed(mode, newMode)) {
            messageText = "/${command.name} "
            mode = newMode
        }
    }

    fun autoCompleteUser(user: User) {
        messageText = "${messageText.substringBeforeLast("@")}@${user.name} "
    }

    /**
     * Get all the attached files with mime-type.
     */
    fun getAttachedFiles(): List<Pair<File, String?>> {
        return selectedAttachments.map { metaData ->
            storageHelper.getCachedFileFromUri(context, metaData) to metaData.mimeType
        }
    }

    fun getCustomAttachments() = selectedCustomAttachments

    fun onReply(replyMessage: Message) {
        mode = Mode.ReplyMessageMode(replyMessage)
    }

    fun onReplyDismissed() {
        if (mode is Mode.ReplyMessageMode) {
            mode = Mode.MessageMode
        }
    }

    fun onEditMessageDismissed() {
        if (mode is Mode.EditMessageMode) {
            mode = Mode.MessageMode
            clearContent()
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

    private fun cancelCustomAttachment(attachment: Attachment) {
        selectedCustomAttachments = selectedCustomAttachments - attachment
        selectedCustomAttachmentsAdapter.removeItem(attachment)

        if (selectedCustomAttachments.isEmpty()) {
            clearSelectedAttachments()
        }

        selectedAttachmentsChanged()
    }

    private fun notifyBigAttachments() {
        _hasBigAttachment.value = selectedAttachments.hasBigAttachment()
    }

    private fun notifySelectedAttachmentsCountChanged() {
        _selectedAttachmentsCount.value = if (selectedAttachments.isNotEmpty()) {
            selectedAttachments.size
        } else {
            selectedCustomAttachments.size
        }
    }

    private fun clearSelectedAttachments() {
        selectedAttachments = emptyList()
        selectedCustomAttachments = emptyList()
        notifyBigAttachments()
        notifySelectedAttachmentsCountChanged()
        binding.selectedFileAttachmentsRecyclerView.isVisible = false
        selectedFileAttachmentAdapter.clear()
        binding.selectedMediaAttachmentsRecyclerView.isVisible = false
        selectedMediaAttachmentAdapter.clear()
        binding.selectedCustomAttachmentsRecyclerView.isVisible = false
        selectedCustomAttachmentsAdapter.clear()
    }

    private fun onModeChanged(currentMode: Mode) {
        when (currentMode) {
            is Mode.FileAttachmentMode -> switchToFileAttachmentMode(currentMode)
            is Mode.MediaAttachmentMode -> switchToMediaAttachmentMode(currentMode)
            is Mode.MessageMode -> switchToMessageMode()
            is Mode.EditMessageMode -> switchToEditMode(currentMode)
            is Mode.CommandMode -> switchToCommandMode(currentMode)
            is Mode.ReplyMessageMode -> switchToReplyMessageMode(currentMode)
            is Mode.CustomAttachmentMode -> switchToCustomAttachmentsMode(currentMode)
        }
        contentChangeListener?.onModeChanged(currentMode)
    }

    /**
     * Switches to reply message mode.
     *
     * @param currentMode [Mode.ReplyMessageMode].
     */
    private fun switchToReplyMessageMode(currentMode: Mode.ReplyMessageMode) {
        switchToMessageMode()
        binding.messageReplyView.setMessage(
            currentMode.repliedMessage,
            ChatClient.instance().getCurrentUser()?.id == currentMode.repliedMessage.user.id,
            messageReplyStyle,
        )
        binding.messageReplyView.isVisible = true
    }

    /**
     * Switches to file attachment mode.
     *
     * @param currentMode [Mode.FileAttachmentMode].
     */
    private fun switchToFileAttachmentMode(mode: Mode.FileAttachmentMode) {
        binding.messageEditText.hint = attachmentModeHint
        selectedAttachments = mode.attachments.toList()
        binding.selectedMediaAttachmentsRecyclerView.isVisible = false
        selectedMediaAttachmentAdapter.clear()
        binding.selectedCustomAttachmentsRecyclerView.isVisible = false
        selectedCustomAttachmentsAdapter.clear()
        binding.selectedFileAttachmentsRecyclerView.isVisible = true
        selectedFileAttachmentAdapter.setItems(selectedAttachments)
        selectedAttachmentsChanged()
    }

    private fun switchToMediaAttachmentMode(mode: Mode.MediaAttachmentMode) {
        binding.messageEditText.hint = attachmentModeHint
        selectedAttachments += mode.attachments.toList()
        binding.selectedFileAttachmentsRecyclerView.isVisible = false
        selectedFileAttachmentAdapter.clear()
        binding.selectedCustomAttachmentsRecyclerView.isVisible = false
        selectedCustomAttachmentsAdapter.clear()
        binding.selectedMediaAttachmentsRecyclerView.isVisible = true
        selectedMediaAttachmentAdapter.setItems(selectedAttachments)
        selectedAttachmentsChanged()
    }

    private fun switchToCustomAttachmentsMode(mode: Mode.CustomAttachmentMode) {
        binding.messageEditText.hint = attachmentModeHint
        selectedCustomAttachments += mode.attachments
        binding.selectedFileAttachmentsRecyclerView.isVisible = false
        selectedFileAttachmentAdapter.clear()
        binding.selectedMediaAttachmentsRecyclerView.isVisible = false
        selectedMediaAttachmentAdapter.clear()
        binding.selectedCustomAttachmentsRecyclerView.isVisible = true
        selectedCustomAttachmentsAdapter.viewHolderFactory = mode.viewHolderFactory
        selectedCustomAttachmentsAdapter.setAttachments(selectedCustomAttachments)
        selectedAttachmentsChanged()
    }

    private fun switchToMessageMode() {
        binding.commandBadge.isVisible = false
        binding.clearCommandButton.isVisible = false
        binding.messageEditText.hint = normalModeHint
        binding.messageReplyView.isVisible = false
    }

    /**
     * Switch to edit mode.
     *
     * @param mode [Mode.EditMessageMode].
     */
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
        if (mode is Mode.CommandMode) {
            resetMode()
        }
    }

    private fun hasValidText(): Boolean = MessageTextValidator.isMessageTextValid(messageText)

    fun hasValidContent(): Boolean {
        return hasValidText() || selectedAttachments.isNotEmpty() || selectedCustomAttachments.isNotEmpty()
    }

    private fun onMessageTextChanged() {
        resetModeIfNecessary()
        contentChangeListener?.onMessageTextChanged(messageText)
    }

    /**
     * Notify that the attachments have changed.
     */
    private fun selectedAttachmentsChanged() {
        notifyBigAttachments()
        notifySelectedAttachmentsCountChanged()
        resetModeIfNecessary()
        contentChangeListener?.onSelectedAttachmentsChanged(selectedAttachments)
        contentChangeListener?.onSelectedCustomAttachmentsChanged(selectedCustomAttachments)
    }

    private fun resetModeIfNecessary() {
        if (!hasValidContent() && (mode is Mode.CustomAttachmentMode || mode is Mode.FileAttachmentMode || mode is Mode.MediaAttachmentMode)) {
            resetMode()
        }
    }

    private fun resetMode() {
        mode = Mode.MessageMode
    }

    interface ContentChangeListener {
        fun onMessageTextChanged(messageText: String)
        fun onSelectedAttachmentsChanged(selectedAttachments: List<AttachmentMetaData>)
        fun onSelectedCustomAttachmentsChanged(selectedCustomAttachments: List<Attachment>)
        fun onModeChanged(mode: Mode)
    }

    sealed class Mode {
        object MessageMode : Mode()
        data class EditMessageMode(val oldMessage: Message) : Mode()
        data class CommandMode(val command: Command) : Mode()
        data class FileAttachmentMode(val attachments: List<AttachmentMetaData>) : Mode()
        data class MediaAttachmentMode(val attachments: List<AttachmentMetaData>) : Mode()

        @ExperimentalStreamChatApi
        data class CustomAttachmentMode(
            val attachments: List<Attachment>,
            val viewHolderFactory: SelectedCustomAttachmentViewHolderFactory,
        ) : Mode()

        data class ReplyMessageMode(val repliedMessage: Message) : Mode()
    }

    private fun List<AttachmentMetaData>.hasBigAttachment() = any { metaData -> metaData.size > attachmentMaxFileSize }
}
