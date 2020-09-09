package com.getstream.sdk.chat.utils

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
import com.getstream.sdk.chat.view.MessageInputStyle
import com.getstream.sdk.chat.view.MessageInputView
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
import java.util.ArrayList
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
    private var mediaAttachmentAdapter: MediaAttachmentAdapter? = null
    private var selectedMediaAttachmentAdapter: MediaAttachmentSelectedAdapter? = null
    private var fileAttachmentAdapter: FileAttachmentListAdapter? = null
    private var selectedFileAttachmentAdapter: AttachmentListAdapter? = null
    private var messageInputType: MessageInputType? = null
    private var selectedAttachments: MutableList<AttachmentMetaData> = ArrayList()
    private var attachmentData: List<AttachmentMetaData> = emptyList()
    private val gridLayoutManager = GridLayoutManager(view.context, 4, RecyclerView.VERTICAL, false)
    private val gridSpacingItemDecoration = GridSpacingItemDecoration(4, 2, false)
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

    internal fun getSelectedAttachments(): List<AttachmentMetaData> {
        return selectedAttachments
    }

    internal fun setSelectedAttachments(selectedAttachments: MutableList<AttachmentMetaData>) {
        this.selectedAttachments = selectedAttachments
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
            MessageInputType.EDIT_MESSAGE -> {
            }
            MessageInputType.ADD_FILE -> {
                if (selectedAttachments.isNotEmpty()) return
                binding.clAddFile.visibility = View.VISIBLE
            }
            MessageInputType.UPLOAD_MEDIA, MessageInputType.UPLOAD_FILE -> {
                binding.clSelectPhoto.visibility = View.VISIBLE
                configAttachmentButtonVisible(false)
            }
            MessageInputType.COMMAND, MessageInputType.MENTION -> {
                binding.btnClose.visibility = View.GONE
            }
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
        configAttachmentButtonVisible(true)
    }

    private fun configSelectAttachView(isMedia: Boolean, treeUri: Uri? = null) {
        GlobalScope.launch(Dispatchers.Main) {
            attachmentData = getAttachmentsFromLocal(isMedia, treeUri)
            if (selectedAttachments.isEmpty()) {
                setAttachmentAdapters(isMedia)
                if (attachmentData.isEmpty()) {
                    Utils.showMessage(
                        view.context,
                        view.context.getResources().getString(R.string.stream_no_media_error)
                    )
                    onClickCloseBackGroundView()
                }
                binding.progressBarFileLoader.visibility = View.GONE
            } else {
                showHideComposerAttachmentGalleryView(true, isMedia)
                setSelectedAttachmentAdapter(false, isMedia)
            }
        }
    }

    private suspend fun getAttachmentsFromLocal(
        isMedia: Boolean,
        treeUri: Uri? = null
    ): List<AttachmentMetaData> =
        withContext(Dispatchers.IO) {
            when (isMedia) {
                true -> StorageUtils.getMediaAttachments(view.context)
                false -> StorageUtils.getFileAttachments(view.context, treeUri)
            }
        }

    private fun setAttachmentAdapters(isMedia: Boolean) {
        if (isMedia) {
            gridSpacingItemDecoration.setSpanCount(MEDIA_ITEMS_PER_ROW)
            gridLayoutManager.spanCount = MEDIA_ITEMS_PER_ROW
            mediaAttachmentAdapter =
                MediaAttachmentAdapter(attachmentData) { updateAttachment(it, isMedia) }
            binding.rvMedia.adapter = mediaAttachmentAdapter
        } else {
            gridSpacingItemDecoration.setSpanCount(FILE_ITEMS_PER_ROW)
            gridLayoutManager.spanCount = FILE_ITEMS_PER_ROW
            fileAttachmentAdapter =
                FileAttachmentListAdapter(attachmentData) { updateAttachment(it, isMedia) }
            binding.rvMedia.adapter = fileAttachmentAdapter
        }
    }

    private fun setSelectedAttachmentAdapter(fromGallery: Boolean, isMedia: Boolean) {
        if (isMedia) {
            selectedMediaAttachmentAdapter = MediaAttachmentSelectedAdapter(
                view.context,
                selectedAttachments,
                MediaAttachmentSelectedAdapter.OnAttachmentCancelListener { attachment: AttachmentMetaData ->
                    cancelAttachment(
                        attachment,
                        fromGallery,
                        isMedia
                    )
                }
            )
            binding.rvComposer.adapter = selectedMediaAttachmentAdapter
        } else {
            selectedFileAttachmentAdapter = AttachmentListAdapter(
                view.context,
                selectedAttachments,
                true,
                false,
                AttachmentListAdapter.OnAttachmentCancelListener { attachment: AttachmentMetaData ->
                    cancelAttachment(
                        attachment,
                        fromGallery,
                        isMedia
                    )
                }
            )
            binding.lvComposer.adapter = selectedFileAttachmentAdapter
        }
    }

    private fun updateAttachment(attachment: AttachmentMetaData, isMedia: Boolean) =
        when (attachment.isSelected) {
            true -> unselectAttachment(attachment, isMedia)
            false -> selectAttachment(attachment, isMedia)
        }

    private fun unselectAttachment(attachment: AttachmentMetaData, isMedia: Boolean) {
        attachment.isSelected = false
        selectedAttachments.remove(attachment)
        selectedAttachmentAdapterChanged(null, true, isMedia)
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
            selectedAttachments.add(attachment)
            showHideComposerAttachmentGalleryView(true, isMedia)
            configSendButtonEnableState()
            selectedAttachmentAdapterChanged(attachment, true, isMedia)
        }
    }

    private fun cancelAttachment(
        attachment: AttachmentMetaData,
        fromGallery: Boolean,
        isMedia: Boolean
    ) {
        attachment.isSelected = false
        selectedAttachments.remove(attachment)
        if (fromGallery) totalAttachmentAdapterChanged(null, isMedia)
        selectedAttachmentAdapterChanged(null, fromGallery, isMedia)
        configSendButtonEnableState()
        if (selectedAttachments.isEmpty() && messageInputType == MessageInputType.EDIT_MESSAGE) configAttachmentButtonVisible(
            true
        )
    }

    private fun configAttachmentButtonVisible(visible: Boolean) {
        if (!style.isShowAttachmentButton) return
        binding.ivOpenAttach.visibility = if (visible) View.VISIBLE else View.GONE
    }

    private fun showHideComposerAttachmentGalleryView(show: Boolean, isMedia: Boolean) {
        if (isMedia) binding.rvComposer.visibility =
            if (show) View.VISIBLE else View.GONE else binding.lvComposer.visibility =
            if (show) View.VISIBLE else View.GONE
    }

    internal fun onCameraClick() {
        if (!PermissionChecker.isGrantedCameraPermissions(view.context)) {
            PermissionChecker.checkCameraPermissions(view) { onCameraClick() }
        } else {
            view.showCameraOptions()
        }
    }

    internal fun onClickOpenSelectView(editAttachments: MutableList<AttachmentMetaData>?, isMedia: Boolean, treeUri: Uri? = null) {
        if (isMedia && !PermissionChecker.isGrantedStoragePermissions(view.context)) {
            PermissionChecker.checkStoragePermissions(view) {
                onClickOpenSelectView(
                    editAttachments,
                    isMedia
                )
            }
            return
        } else if (!isMedia && treeUri == null) {
            Utils.showMessage(view.context, R.string.stream_permissions_storage_message)
            return
        }
        initAdapter()
        if (editAttachments != null && editAttachments.isNotEmpty()) setSelectedAttachments(
            editAttachments
        )
        configSelectAttachView(isMedia, treeUri)
        if (selectedAttachments.isEmpty()) {
            binding.progressBarFileLoader.visibility = View.VISIBLE
            onClickOpenBackGroundView(if (isMedia) MessageInputType.UPLOAD_MEDIA else MessageInputType.UPLOAD_FILE)
        }
    }

    private fun totalAttachmentAdapterChanged(attachment: AttachmentMetaData?, isMedia: Boolean) {
        if (isMedia) {
            if (attachment == null) {
                mediaAttachmentAdapter?.notifyDataSetChanged()
                return
            }
            val index = attachmentData.indexOf(attachment)
            if (index != -1) mediaAttachmentAdapter?.notifyItemChanged(index)
        } else fileAttachmentAdapter?.notifyDataSetChanged()
    }

    private fun selectedAttachmentAdapterChanged(
        attachment: AttachmentMetaData?,
        fromGallery: Boolean,
        isMedia: Boolean
    ) {
        if (isMedia) {
            if (selectedMediaAttachmentAdapter == null) setSelectedAttachmentAdapter(
                fromGallery,
                isMedia
            )
            if (attachment == null) {
                selectedMediaAttachmentAdapter!!.notifyDataSetChanged()
                return
            }
            val index = selectedAttachments.indexOf(attachment)
            if (index != -1) selectedMediaAttachmentAdapter!!.notifyItemChanged(index)
        } else {
            if (selectedFileAttachmentAdapter == null) setSelectedAttachmentAdapter(
                fromGallery,
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
        initAdapter()
        onClickCloseBackGroundView()
    }

    private fun initAdapter() {
        binding.lvComposer.removeAllViewsInLayout()
        binding.rvComposer.removeAllViewsInLayout()
        binding.lvComposer.visibility = View.GONE
        binding.rvComposer.visibility = View.GONE
        binding.rvMedia.layoutManager = gridLayoutManager
        binding.rvMedia.addItemDecoration(gridSpacingItemDecoration)
        mediaAttachmentAdapter?.clear()
        fileAttachmentAdapter?.clear()
        selectedAttachments.clear()
        mediaAttachmentAdapter = null
        selectedMediaAttachmentAdapter = null
        fileAttachmentAdapter = null
        selectedFileAttachmentAdapter = null
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
