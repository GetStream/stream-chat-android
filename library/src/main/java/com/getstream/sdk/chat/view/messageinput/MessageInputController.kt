package com.getstream.sdk.chat.view.messageinput

import android.net.Uri
import android.view.View
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.getstream.sdk.chat.R
import com.getstream.sdk.chat.adapter.AttachmentListAdapter
import com.getstream.sdk.chat.adapter.FileAttachmentListAdapter
import com.getstream.sdk.chat.adapter.MediaAttachmentAdapter
import com.getstream.sdk.chat.adapter.MediaAttachmentSelectedAdapter
import com.getstream.sdk.chat.databinding.StreamViewMessageInputBinding
import com.getstream.sdk.chat.enums.MessageInputType
import com.getstream.sdk.chat.exhaustive
import com.getstream.sdk.chat.model.AttachmentMetaData
import com.getstream.sdk.chat.utils.Constant
import com.getstream.sdk.chat.utils.GridSpacingItemDecoration
import com.getstream.sdk.chat.utils.PermissionChecker
import com.getstream.sdk.chat.utils.StorageUtils
import com.getstream.sdk.chat.utils.StringUtility
import com.getstream.sdk.chat.utils.Utils
import com.getstream.sdk.chat.view.PreviewMessageView
import com.getstream.sdk.chat.view.common.visible
import io.getstream.chat.android.client.models.Command
import io.getstream.chat.android.client.models.Member
import io.getstream.chat.android.client.models.Message
import io.getstream.chat.android.client.models.User
import io.getstream.chat.android.client.models.name
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.util.regex.Pattern
import kotlin.properties.Delegates

private val COMMAND_PATTERN = Pattern.compile("^/[a-z]*$")
private val MENTION_PATTERN = Pattern.compile("^(.* )?@([a-zA-Z]+[0-9]*)*$")
private const val MEDIA_ITEMS_PER_ROW = 4
private const val FILE_ITEMS_PER_ROW = 1

internal class MessageInputController(
    private val binding: StreamViewMessageInputBinding,
    private val view: MessageInputView,
    private val style: MessageInputStyle
) {

    private val gridLayoutManager = GridLayoutManager(view.context, 4, RecyclerView.VERTICAL, false)
    private val gridSpacingItemDecoration = GridSpacingItemDecoration(4, 2, false)

    init {
        binding.rvMedia.layoutManager = gridLayoutManager
        binding.rvMedia.addItemDecoration(gridSpacingItemDecoration)
    }

    private var totalMediaAttachmentAdapter: MediaAttachmentAdapter? = null
    private var selectedMediaAttachmentAdapter: MediaAttachmentSelectedAdapter? = null
    private var totalFileAttachmentAdapter: FileAttachmentListAdapter? = null
    private var selectedFileAttachmentAdapter: AttachmentListAdapter? = null
    private var messageInputType: MessageInputType? = null
    private var selectedAttachments: Set<AttachmentMetaData> = emptySet()
    private var totalAttachments: Set<AttachmentMetaData> = emptySet()
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
        binding.vPreviewMessage.visible(false)
        binding.ivOpenAttach.visible(style.isShowAttachmentButton)
        binding.cbSendAlsoToChannel.visible(true)
        binding.cbSendAlsoToChannel.isChecked = false
    }

    private fun configureNormalInputMode() {
        binding.vPreviewMessage.visible(false)
        binding.ivOpenAttach.visible(style.isShowAttachmentButton)
        binding.cbSendAlsoToChannel.visible(false)
    }

    private fun configureEditInputMode(message: Message) {
        binding.vPreviewMessage.setMessage(message, PreviewMessageView.Mode.EDIT)
        binding.vPreviewMessage.onCloseClick = {
            inputMode = InputMode.Normal
            binding.messageTextInput.setText("")
        }
        binding.messageTextInput.setText(message.text)
        binding.vPreviewMessage.visible(true)
        binding.ivOpenAttach.visible(false)
        binding.cbSendAlsoToChannel.visible(false)
    }

    internal fun getSelectedAttachments(): Set<AttachmentMetaData> {
        return selectedAttachments
    }

    internal fun setSelectedAttachments(selectedAttachments: Set<AttachmentMetaData>) {
        this.selectedAttachments += selectedAttachments
    }

    internal fun onSendMessageClick(message: String) = when (val im = inputMode) {
        is InputMode.Normal -> sendNormalMessage(message)
        is InputMode.Thread -> sendToThread(im.parentMessage, message)
        is InputMode.Edit -> editMessage(im.oldMessage, message).also {
            inputMode = InputMode.Normal
        }
    }

    private fun sendNormalMessage(message: String) = when (selectedAttachments.isEmpty()) {
        true -> view.sendTextMessage(message)
        false -> view.sendAttachments(
            message,
            selectedAttachments.map { StorageUtils.getCachedFileFromUri(view.context, it) }
        )
    }

    private fun sendToThread(parentMessage: Message, message: String) =
        when (selectedAttachments.isEmpty()) {
            true -> view.sendToThread(parentMessage, message, binding.cbSendAlsoToChannel.isChecked)
            false -> view.sendToThreadWithAttachments(
                parentMessage,
                message,
                binding.cbSendAlsoToChannel.isChecked,
                selectedAttachments.map { StorageUtils.getCachedFileFromUri(view.context, it) }
            )
        }

    private fun editMessage(message: Message, messageText: String) {
        view.editMessage(message, messageText)
    }

    internal fun onClickOpenBackGroundView(type: MessageInputType) {
        binding.root.setBackgroundResource(R.drawable.stream_round_thread_toolbar)
        binding.clTitle.visibility = View.VISIBLE
        binding.btnClose.visibility = View.VISIBLE
        binding.clAddFile.visibility = View.GONE
        binding.clSelectPhoto.visibility = View.GONE
        when (type) {
            MessageInputType.EDIT_MESSAGE -> Unit
            MessageInputType.ADD_FILE -> binding.clAddFile.visibility = View.VISIBLE
            MessageInputType.UPLOAD_MEDIA, MessageInputType.UPLOAD_FILE -> {
                binding.clSelectPhoto.visibility = View.VISIBLE
                configAttachmentButtonVisible(false)
            }
            MessageInputType.COMMAND, MessageInputType.MENTION -> binding.btnClose.visibility = View.GONE
        }
        binding.tvTitle.text = type.getLabel(view.context)
        messageInputType = type
        configPermissions()
    }

    private fun configPermissions() {
        when {
            PermissionChecker.isGrantedCameraPermissions(view.context) -> {
                binding.ivMediaPermission.visibility = View.GONE
                binding.ivCameraPermission.visibility = View.GONE
                binding.ivFilePermission.visibility = View.VISIBLE
            }
            PermissionChecker.isGrantedStoragePermissions(view.context) -> {
                binding.ivMediaPermission.visibility = View.GONE
                binding.ivCameraPermission.visibility = View.VISIBLE
                binding.ivFilePermission.visibility = View.VISIBLE
            }
            else -> {
                binding.ivMediaPermission.visibility = View.VISIBLE
                binding.ivCameraPermission.visibility = View.VISIBLE
                binding.ivFilePermission.visibility = View.VISIBLE
            }
        }
    }

    internal fun onClickCloseBackGroundView() {
        binding.clTitle.visibility = View.GONE
        binding.clAddFile.visibility = View.GONE
        binding.clSelectPhoto.visibility = View.GONE
        binding.root.setBackgroundResource(0)
        messageInputType = null
        totalAttachments = emptySet()
        configAttachmentButtonVisible(true)
    }

    private fun configSelectAttachView(isMedia: Boolean, treeUri: Uri? = null) {
        GlobalScope.launch(Dispatchers.Main) {
            binding.progressBarFileLoader.visibility = View.VISIBLE
            totalAttachments = getAttachmentsFromLocal(isMedia, treeUri)
            if (selectedAttachments.isEmpty()) {
                setTotalAttachmentAdapters(isMedia, totalAttachments.toList())
                if (totalAttachments.isEmpty()) {
                    Utils.showMessage(view.context, R.string.stream_no_media_error)
                    onClickCloseBackGroundView()
                }
            } else {
                showComposerAttachmentGalleryView(isMedia)
                setSelectedAttachmentAdapter(false, isMedia)
            }

            binding.progressBarFileLoader.visibility = View.GONE
        }
    }

    private suspend fun getAttachmentsFromLocal(
        isMedia: Boolean,
        treeUri: Uri? = null
    ): Set<AttachmentMetaData> =
        withContext(Dispatchers.IO) {
            when (isMedia) {
                true -> StorageUtils.getMediaAttachments(view.context).toSet()
                false -> StorageUtils.getFileAttachments(view.context, treeUri).toSet()
            }
        }

    private fun setTotalAttachmentAdapters(isMedia: Boolean, totalAttachment: List<AttachmentMetaData>) {
        if (isMedia) {
            gridSpacingItemDecoration.setSpanCount(MEDIA_ITEMS_PER_ROW)
            gridLayoutManager.spanCount = MEDIA_ITEMS_PER_ROW
            totalMediaAttachmentAdapter =
                MediaAttachmentAdapter(totalAttachment) { updateAttachment(it, isMedia) }
            binding.rvMedia.adapter = totalMediaAttachmentAdapter
        } else {
            gridSpacingItemDecoration.setSpanCount(FILE_ITEMS_PER_ROW)
            gridLayoutManager.spanCount = FILE_ITEMS_PER_ROW
            totalFileAttachmentAdapter =
                FileAttachmentListAdapter(totalAttachment) { updateAttachment(it, isMedia) }
            binding.rvMedia.adapter = totalFileAttachmentAdapter
        }
    }

    private fun setSelectedAttachmentAdapter(fromGallery: Boolean, isMedia: Boolean) {
        if (isMedia) {
            selectedMediaAttachmentAdapter = MediaAttachmentSelectedAdapter(
                selectedAttachments.toList(),
                object : MediaAttachmentSelectedAdapter.OnAttachmentCancelListener {
                    override fun onCancel(attachment: AttachmentMetaData) {
                        cancelAttachment(
                            attachment,
                            fromGallery,
                            isMedia
                        )
                    }
                }
            )
            binding.mediaComposer.adapter = selectedMediaAttachmentAdapter
            binding.mediaComposer.visibility = View.VISIBLE
            binding.fileComposer.visibility = View.GONE
            binding.fileComposer.adapter = null
            selectedFileAttachmentAdapter?.clear()
            selectedFileAttachmentAdapter = null
        } else {
            selectedFileAttachmentAdapter = AttachmentListAdapter(
                view.context,
                selectedAttachments.toList(),
                true,
                false,
                { attachment: AttachmentMetaData ->
                    cancelAttachment(
                        attachment,
                        fromGallery,
                        isMedia
                    )
                }
            )
            binding.fileComposer.adapter = selectedFileAttachmentAdapter
            binding.fileComposer.visibility = View.VISIBLE
            binding.mediaComposer.visibility = View.GONE
            binding.mediaComposer.adapter = null
            selectedMediaAttachmentAdapter?.clear()
            selectedMediaAttachmentAdapter = null
        }
    }

    private fun updateAttachment(attachment: AttachmentMetaData, isMedia: Boolean) =
        when (attachment.isSelected) {
            true -> unselectAttachment(attachment, isMedia)
            false -> selectAttachment(attachment, isMedia)
        }

    private fun unselectAttachment(attachment: AttachmentMetaData, isMedia: Boolean) {
        attachment.isSelected = false
        selectedAttachments = selectedAttachments - attachment
        removeAttachmentFromAdapters(attachment, true, isMedia)
        configSendButtonEnableState()
        if (selectedAttachments.isEmpty() && messageInputType == MessageInputType.EDIT_MESSAGE) configAttachmentButtonVisible(
            true
        )
    }

    private fun selectAttachment(attachment: AttachmentMetaData, isMedia: Boolean) {
        if (attachment.size > Constant.MAX_UPLOAD_FILE_SIZE) {
            Utils.showMessage(view.context, R.string.stream_large_size_file_error)
        } else {
            attachment.isSelected = true
            selectedAttachments = selectedAttachments + attachment
            showComposerAttachmentGalleryView(isMedia)
            configSendButtonEnableState()
            addAttachmentToAdapter(attachment, isMedia)
        }
    }

    private fun cancelAttachment(
        attachment: AttachmentMetaData,
        fromGallery: Boolean,
        isMedia: Boolean
    ) {
        selectedAttachments = selectedAttachments - attachment
        if (fromGallery) totalAttachmentAdapterChanged(isMedia)
        removeAttachmentFromAdapters(attachment, fromGallery, isMedia)
        configSendButtonEnableState()
        if (selectedAttachments.isEmpty() && messageInputType == MessageInputType.EDIT_MESSAGE) configAttachmentButtonVisible(
            true
        )
    }

    private fun configAttachmentButtonVisible(visible: Boolean) {
        if (!style.isShowAttachmentButton) return
        binding.ivOpenAttach.visibility = if (visible) View.VISIBLE else View.GONE
    }

    private fun showComposerAttachmentGalleryView(isMedia: Boolean) {
        if (isMedia) {
            binding.mediaComposer.visibility = View.VISIBLE
        } else {
            binding.fileComposer.visibility = View.VISIBLE
        }
    }

    internal fun onCameraClick() {
        if (!PermissionChecker.isGrantedCameraPermissions(view.context)) {
            PermissionChecker.checkCameraPermissions(view) { onCameraClick() }
        } else {
            view.showCameraOptions()
        }
    }

    internal fun onClickOpenSelectView(isMedia: Boolean, treeUri: Uri? = null) =
        openSelectView(selectedAttachments, isMedia, treeUri)

    private fun openSelectView(
        editAttachments: Set<AttachmentMetaData>,
        isMedia: Boolean,
        treeUri: Uri? = null
    ) {
        if (isMedia && !PermissionChecker.isGrantedStoragePermissions(view.context)) {
            PermissionChecker.checkStoragePermissions(view) {
                openSelectView(editAttachments, isMedia)
            }
            return
        } else if (!isMedia && treeUri == null) {
            Utils.showMessage(view.context, R.string.stream_permissions_storage_message)
            return
        }
        if (editAttachments.isNotEmpty()) {
            setSelectedAttachments(editAttachments)
        }
        configSelectAttachView(isMedia, treeUri)
        onClickOpenBackGroundView(if (isMedia) MessageInputType.UPLOAD_MEDIA else MessageInputType.UPLOAD_FILE)
    }

    private fun totalAttachmentAdapterChanged(isMedia: Boolean) {
        if (isMedia) totalMediaAttachmentAdapter?.notifyDataSetChanged() else totalFileAttachmentAdapter?.notifyDataSetChanged()
    }

    private fun removeAttachmentFromAdapters(
        attachment: AttachmentMetaData,
        fromGallery: Boolean,
        isMedia: Boolean
    ) {
        if (isMedia) {
            selectedMediaAttachmentAdapter?.removeAttachment(attachment) ?: setSelectedAttachmentAdapter(
                fromGallery,
                isMedia
            )
            totalMediaAttachmentAdapter?.unselectAttachment(attachment) ?: setTotalAttachmentAdapters(
                isMedia,
                totalAttachments.toList()
            )
        } else {
            if (selectedFileAttachmentAdapter == null) setSelectedAttachmentAdapter(
                fromGallery,
                isMedia
            )
            selectedFileAttachmentAdapter!!.notifyDataSetChanged()
        }
    }

    private fun addAttachmentToAdapter(
        attachment: AttachmentMetaData,
        isMedia: Boolean
    ) {
        if (isMedia) {
            selectedMediaAttachmentAdapter?.addAttachment(attachment) ?: setSelectedAttachmentAdapter(
                true,
                isMedia
            )
            totalMediaAttachmentAdapter?.selectAttachment(attachment) ?: setTotalAttachmentAdapters(
                isMedia,
                totalAttachments.toList()
            )
        } else {
            if (selectedFileAttachmentAdapter == null) setSelectedAttachmentAdapter(
                true,
                isMedia
            )
            selectedFileAttachmentAdapter!!.notifyDataSetChanged()
        }
    }

    private fun configSendButtonEnableState() {
        if (!StringUtility.isEmptyTextMessage(binding.messageTextInput.text.toString())) {
            binding.activeMessageSend = true
        } else {
            binding.activeMessageSend = selectedAttachments.isNotEmpty()
        }
    }

    internal fun initSendMessage() {
        binding.messageTextInput.setText("")
        disposeAdapters()
        onClickCloseBackGroundView()
    }

    private fun disposeAdapters() {
        binding.fileComposer.visibility = View.GONE
        binding.mediaComposer.visibility = View.GONE
        totalMediaAttachmentAdapter?.clear()
        totalFileAttachmentAdapter?.clear()
        selectedFileAttachmentAdapter?.clear()
        selectedMediaAttachmentAdapter?.clear()
    }

    internal fun onFileCaptured(file: File) {
        selectAttachment(AttachmentMetaData(file), true)
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
