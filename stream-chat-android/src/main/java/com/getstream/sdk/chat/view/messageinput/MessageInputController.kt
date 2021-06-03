package com.getstream.sdk.chat.view.messageinput

import android.content.Context
import android.net.Uri
import android.view.View
import androidx.core.view.isVisible
import com.getstream.sdk.chat.adapter.FileAttachmentSelectedAdapter
import com.getstream.sdk.chat.adapter.MediaAttachmentAdapter
import com.getstream.sdk.chat.adapter.MediaAttachmentSelectedAdapter
import com.getstream.sdk.chat.databinding.StreamViewMessageInputBinding
import com.getstream.sdk.chat.enums.MessageInputType
import com.getstream.sdk.chat.enums.label
import com.getstream.sdk.chat.model.AttachmentMetaData
import com.getstream.sdk.chat.utils.PermissionChecker
import com.getstream.sdk.chat.utils.StorageHelper
import com.getstream.sdk.chat.view.PreviewMessageView
import io.getstream.chat.android.client.models.Command
import io.getstream.chat.android.client.models.Member
import io.getstream.chat.android.client.models.Message
import io.getstream.chat.android.client.models.User
import io.getstream.chat.android.client.models.name
import io.getstream.chat.android.core.internal.exhaustive
import java.io.File
import java.util.regex.Pattern
import kotlin.properties.Delegates

private val COMMAND_PATTERN = Pattern.compile("^/[a-z]*$")
private val MENTION_PATTERN = Pattern.compile("^(.* )?@([a-zA-Z]+[0-9]*)*$")

internal class MessageInputController(
    private val binding: StreamViewMessageInputBinding,
    private val view: MessageInputView,
    private val style: MessageInputStyle
) {

    private val storageHelper = StorageHelper()

    internal val attachmentsController =
        AttachmentsController(
            this,
            PermissionChecker(),
            storageHelper,
            view,
            MediaAttachmentAdapter(),
            MediaAttachmentSelectedAdapter(),
            FileAttachmentSelectedAdapter(emptyList(), true),
            style.isShowAttachmentButton
        )

    private var messageInputType: MessageInputType? = null
    internal var members: List<Member> = listOf()
    internal var channelCommands: List<Command> = listOf()
    internal var inputMode: InputMode by Delegates.observable(InputMode.Normal as InputMode) { _, _, newValue ->
        when (newValue) {
            is InputMode.Normal -> configureNormalInputMode()
            is InputMode.Thread -> configureThreadInputMode()
            is InputMode.Edit -> configureEditInputMode(newValue.oldMessage)
        }.exhaustive
    }

    private fun configureThreadInputMode() {
        binding.vPreviewMessage.isVisible = false
        binding.ivOpenAttach.isVisible = style.isShowAttachmentButton
        binding.cbSendAlsoToChannel.isVisible = style.sendAlsoToChannelCheckboxEnabled
        binding.cbSendAlsoToChannel.isChecked = false
    }

    private fun configureNormalInputMode() {
        binding.vPreviewMessage.isVisible = false
        binding.ivOpenAttach.isVisible = style.isShowAttachmentButton
        binding.cbSendAlsoToChannel.isVisible = false
    }

    private fun configureEditInputMode(message: Message) {
        binding.vPreviewMessage.setMessage(message, PreviewMessageView.Mode.EDIT)
        binding.vPreviewMessage.onCloseClick = {
            inputMode = InputMode.Normal
            binding.messageTextInput.setText("")
        }
        binding.messageTextInput.setText(message.text)
        binding.vPreviewMessage.isVisible = true
        binding.ivOpenAttach.isVisible = false
        binding.cbSendAlsoToChannel.isVisible = false
    }

    internal fun onSendMessageClick(message: String) = when (val im = inputMode) {
        is InputMode.Normal -> sendNormalMessage(message)
        is InputMode.Thread -> sendToThread(im.parentMessage, message)
        is InputMode.Edit -> editMessage(im.oldMessage, message).also {
            inputMode = InputMode.Normal
        }
    }

    private fun sendNormalMessage(message: String) =
        when (attachmentsController.selectedAttachments.isEmpty()) {
            true -> view.sendTextMessage(message)
            false -> view.sendAttachments(
                message,
                attachmentsController.selectedAttachments.map { metaData ->
                    storageHelper.getCachedFileFromUri(
                        view.context,
                        metaData
                    ) to metaData.mimeType
                }
            )
        }

    private fun sendToThread(parentMessage: Message, message: String) =
        when (attachmentsController.selectedAttachments.isEmpty()) {
            true -> view.sendToThread(parentMessage, message, binding.cbSendAlsoToChannel.isChecked)
            false -> view.sendToThreadWithAttachments(
                parentMessage,
                message,
                binding.cbSendAlsoToChannel.isChecked,
                attachmentsController.selectedAttachments.map {
                    storageHelper.getCachedFileFromUri(
                        view.context,
                        it
                    )
                }
            )
        }

    private fun editMessage(message: Message, messageText: String) {
        view.editMessage(message, messageText)
    }

    internal fun onClickCloseAttachmentSelectionMenu() {
        messageInputType = null
        attachmentsController.onClickCloseAttachmentSelectionMenu()
    }

    internal fun onClickOpenAttachmentSelectionMenu(type: MessageInputType) {
        attachmentsController.onClickOpenAttachmentSelectionMenu()
        when (type) {
            MessageInputType.EDIT_MESSAGE -> Unit
            MessageInputType.ADD_FILE -> binding.clAddFile.visibility = View.VISIBLE
            MessageInputType.UPLOAD_MEDIA, MessageInputType.UPLOAD_FILE -> {
                binding.clSelectPhoto.visibility = View.VISIBLE
                attachmentsController.configAttachmentButtonVisible(false)
            }
            MessageInputType.COMMAND, MessageInputType.MENTION ->
                binding.btnClose.visibility =
                    View.GONE
        }
        binding.tvTitle.text = type.label
    }

    internal fun configSendButtonEnableState() {
        if (binding.messageTextInput.text.toString().isNotBlank()) {
            binding.sendButton.isVisible = true
        } else {
            binding.sendButton.isVisible = attachmentsController.selectedAttachments.isNotEmpty()
        }
    }

    internal fun initSendMessage() {
        binding.messageTextInput.setText("")
        attachmentsController.clearState()
        onClickCloseAttachmentSelectionMenu()
    }

    internal fun onFileCaptured(context: Context, file: File) {
        attachmentsController.selectAttachmentFromCamera(AttachmentMetaData(context, file))
    }

    internal fun onFilesSelected(uriList: List<Uri>) {
        attachmentsController.selectAttachmentsFromUriList(uriList)
    }

    internal fun checkCommandsOrMentions(inputMessage: String) {
        when {
            inputMessage.isCommandMessage() -> {
                view.showSuggestedCommand(channelCommands.matchName(inputMessage.removePrefix("/")))
            }
            inputMessage.isMentionMessage() -> {
                view.showSuggestedMentions(members.matchUserName(inputMessage.substringAfterLast("@")))
            }
            else -> {
                cleanSuggestion()
            }
        }
    }

    private fun cleanSuggestion() {
        view.showSuggestedMentions(listOf())
        view.showSuggestedCommand(listOf())
    }

    internal fun onCommandSelected(command: Command) {
        view.messageText = "/${command.name} "
    }

    internal fun onUserSelected(currentMessage: String, user: User) {
        view.messageText = "${currentMessage.substringBeforeLast("@")}@${user.name} "
    }

    fun getSelectedAttachments() = attachmentsController.selectedAttachments
    fun onClickOpenMediaSelectView() {
        messageInputType = MessageInputType.UPLOAD_MEDIA
        attachmentsController.onClickOpenMediaSelectView(messageInputType!!)
        onClickOpenAttachmentSelectionMenu(messageInputType!!)
    }

    fun onCameraClick() = attachmentsController.onCameraClick()
    fun setSelectedAttachments(attachments: Set<AttachmentMetaData>) =
        attachmentsController.setSelectedAttachments(attachments)
}

internal sealed class InputMode {
    object Normal : InputMode()
    data class Thread(val parentMessage: Message) : InputMode()
    data class Edit(val oldMessage: Message) : InputMode()
}

private fun String.isCommandMessage() = COMMAND_PATTERN.matcher(this).find()
private fun String.isMentionMessage() = MENTION_PATTERN.matcher(this).find()
private fun List<Command>.matchName(namePattern: String) =
    filter { it.name.startsWith(namePattern) }

private fun List<Member>.matchUserName(namePattern: String): List<User> = map { it.user }
    .filter { it.name.contains(namePattern, true) }
