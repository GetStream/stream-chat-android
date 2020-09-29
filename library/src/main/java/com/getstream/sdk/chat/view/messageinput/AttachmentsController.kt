package com.getstream.sdk.chat.view.messageinput

import android.net.Uri
import com.getstream.sdk.chat.R
import com.getstream.sdk.chat.adapter.FileAttachmentListAdapter
import com.getstream.sdk.chat.adapter.FileAttachmentSelectedAdapter
import com.getstream.sdk.chat.adapter.MediaAttachmentAdapter
import com.getstream.sdk.chat.adapter.MediaAttachmentSelectedAdapter
import com.getstream.sdk.chat.enums.MessageInputType
import com.getstream.sdk.chat.model.AttachmentMetaData
import com.getstream.sdk.chat.utils.Constant
import com.getstream.sdk.chat.utils.PermissionChecker
import com.getstream.sdk.chat.utils.StorageUtils
import com.getstream.sdk.chat.utils.Utils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

internal class AttachmentsController(
    private val rootController: MessageInputController,
    private val view: MessageInputView,
    private val showOpenAttachmentsMenuConfig: Boolean
) {

    private var totalMediaAttachmentAdapter: MediaAttachmentAdapter? = null
    private var selectedMediaAttachmentAdapter: MediaAttachmentSelectedAdapter? = null
    private var totalFileAttachmentAdapter: FileAttachmentListAdapter? = null
    private var selectedFileAttachmentAdapter: FileAttachmentSelectedAdapter? = null

    internal var selectedAttachments: Set<AttachmentMetaData> = emptySet()
        private set
    private var totalAttachments: Set<AttachmentMetaData> = emptySet()

    internal fun setSelectedAttachments(selectedAttachments: Set<AttachmentMetaData>) {
        this.selectedAttachments += selectedAttachments
    }

    internal fun onClickCloseAttachmentSelectionMenu() {
        view.hideAttachmentsMenu()
        totalAttachments = emptySet()
        totalFileAttachmentAdapter?.clear()
        totalMediaAttachmentAdapter?.clear()
        totalFileAttachmentAdapter = null
        totalMediaAttachmentAdapter = null
        configAttachmentButtonVisible(true)
    }

    private fun fillTotalAttachmentsView(
        messageInputType: MessageInputType?,
        isMedia: Boolean,
        treeUri: Uri? = null
    ) {
        GlobalScope.launch(Dispatchers.Main) {
            view.showLoadingTotalAttachments(true)
            totalAttachments = getAttachmentsFromLocal(isMedia, treeUri)
            if (totalAttachments.isEmpty()) {
                Utils.showMessage(view.context, R.string.stream_no_media_error)
                onClickCloseAttachmentSelectionMenu()
            } else {
                setTotalAttachmentAdapters(
                    totalAttachments.toList(),
                    selectedAttachments.toList(),
                    messageInputType,
                    isMedia
                )
            }
            setSelectedAttachmentAdapter(messageInputType, isMedia)
            view.showLoadingTotalAttachments(false)
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

    internal fun configAttachmentButtonVisible(visible: Boolean) {
        if (!showOpenAttachmentsMenuConfig) return
        view.showOpenAttachmentsMenuButton(visible)
    }

    private fun cancelAttachment(
        attachment: AttachmentMetaData,
        messageInputType: MessageInputType?,
        isMedia: Boolean
    ) {
        selectedAttachments = selectedAttachments - attachment
        removeAttachmentFromAdapters(attachment, messageInputType, isMedia)
        rootController.configSendButtonEnableState()
        if (selectedAttachments.isEmpty() && messageInputType == MessageInputType.EDIT_MESSAGE) configAttachmentButtonVisible(
            true
        )
    }

    private fun showSelectedAttachments(isMedia: Boolean) {
        if (isMedia) {
            view.showMediaAttachments()
        } else {
            view.showFileAttachments()
        }
    }

    internal fun onCameraClick() {
        if (!PermissionChecker.isGrantedCameraPermissions(view.context)) {
            PermissionChecker.checkCameraPermissions(view) { onCameraClick() }
        } else {
            view.showCameraOptions()
        }
    }

    internal fun onClickOpenSelectView(messageInputType: MessageInputType?, isMedia: Boolean, treeUri: Uri? = null) =
        openSelectView(selectedAttachments, messageInputType, isMedia, treeUri)

    private fun openSelectView(
        editAttachments: Set<AttachmentMetaData>,
        messageInputType: MessageInputType?,
        isMedia: Boolean,
        treeUri: Uri? = null
    ) {
        if (isMedia && !PermissionChecker.isGrantedStoragePermissions(view.context)) {
            PermissionChecker.checkStoragePermissions(view) {
                openSelectView(editAttachments, messageInputType, isMedia)
            }
            return
        } else if (!isMedia && treeUri == null) {
            Utils.showMessage(view.context, R.string.stream_permissions_storage_message)
            return
        }
        setSelectedAttachments(editAttachments)
        fillTotalAttachmentsView(messageInputType, isMedia, treeUri)
    }

    private fun removeAttachmentFromAdapters(
        attachment: AttachmentMetaData,
        messageInputType: MessageInputType?,
        isMedia: Boolean
    ) {
        if (isMedia) {
            selectedMediaAttachmentAdapter?.removeAttachment(attachment) ?: setSelectedAttachmentAdapter(
                messageInputType,
                isMedia
            )
            totalMediaAttachmentAdapter?.unselectAttachment(attachment) ?: setTotalAttachmentAdapters(
                totalAttachments.toList(),
                selectedAttachments.toList(),
                messageInputType,
                isMedia
            )
        } else {
            selectedFileAttachmentAdapter?.setAttachments(selectedAttachments.toList()) ?: setSelectedAttachmentAdapter(
                messageInputType,
                isMedia
            )
        }
    }

    private fun addAttachmentToAdapter(
        attachment: AttachmentMetaData,
        messageInputType: MessageInputType?,
        isMedia: Boolean
    ) {
        if (isMedia) {
            selectedMediaAttachmentAdapter?.addAttachment(attachment) ?: setSelectedAttachmentAdapter(
                messageInputType,
                isMedia
            )
            totalMediaAttachmentAdapter?.selectAttachment(attachment) ?: setTotalAttachmentAdapters(
                totalAttachments.toList(),
                selectedAttachments.toList(),
                messageInputType,
                isMedia
            )
        } else {
            selectedFileAttachmentAdapter?.setAttachments(selectedAttachments.toList()) ?: setSelectedAttachmentAdapter(
                messageInputType,
                isMedia
            )
        }
    }

    private fun setTotalAttachmentAdapters(
        totalAttachment: List<AttachmentMetaData>,
        selectedAttachments: List<AttachmentMetaData>,
        messageInputType: MessageInputType?,
        isMedia: Boolean
    ) {
        totalAttachment.forEach { it.isSelected = selectedAttachments.contains(it) }
        if (isMedia) {
            totalMediaAttachmentAdapter =
                MediaAttachmentAdapter(totalAttachment) { updateAttachment(it, messageInputType, isMedia) }
            view.showTotalMediaAttachments(totalMediaAttachmentAdapter!!)
        } else {
            totalFileAttachmentAdapter =
                FileAttachmentListAdapter(totalAttachment) { updateAttachment(it, messageInputType, isMedia) }
            view.showTotalFileAttachments(totalFileAttachmentAdapter!!)
        }
    }

    private fun setSelectedAttachmentAdapter(
        messageInputType: MessageInputType?,
        isMedia: Boolean
    ) {
        if (isMedia) {
            selectedMediaAttachmentAdapter = MediaAttachmentSelectedAdapter(
                selectedAttachments.toList(),
                object : MediaAttachmentSelectedAdapter.OnAttachmentCancelListener {
                    override fun onCancel(attachment: AttachmentMetaData) {
                        cancelAttachment(
                            attachment,
                            messageInputType,
                            isMedia
                        )
                    }
                }
            )
            view.showSelectedMediaAttachments(selectedMediaAttachmentAdapter!!)
            selectedFileAttachmentAdapter?.clear()
            selectedFileAttachmentAdapter = null
        } else {
            selectedFileAttachmentAdapter = FileAttachmentSelectedAdapter(
                selectedAttachments.toList(),
                true,
                object : FileAttachmentSelectedAdapter.OnAttachmentCancelListener {
                    override fun onCancel(attachment: AttachmentMetaData) {
                        cancelAttachment(
                            attachment,
                            messageInputType,
                            isMedia
                        )
                    }
                }
            )
            view.showSelectedFileAttachments(selectedFileAttachmentAdapter!!)
            selectedMediaAttachmentAdapter?.clear()
            selectedMediaAttachmentAdapter = null
        }
    }

    private fun updateAttachment(
        attachment: AttachmentMetaData,
        messageInputType: MessageInputType?,
        isMedia: Boolean
    ) = when (attachment.isSelected) {
        true -> unselectAttachment(attachment, messageInputType, isMedia)
        false -> selectAttachment(attachment, messageInputType, isMedia)
    }

    private fun unselectAttachment(
        attachment: AttachmentMetaData,
        messageInputType: MessageInputType?,
        isMedia: Boolean
    ) {
        attachment.isSelected = false
        selectedAttachments = selectedAttachments - attachment
        removeAttachmentFromAdapters(attachment, messageInputType, isMedia)
        rootController.configSendButtonEnableState()
        if (selectedAttachments.isEmpty() && messageInputType == MessageInputType.EDIT_MESSAGE) {
            configAttachmentButtonVisible(true)
        }
    }

    internal fun selectAttachment(
        attachment: AttachmentMetaData,
        messageInputType: MessageInputType?,
        isMedia: Boolean
    ) {
        if (attachment.size > Constant.MAX_UPLOAD_FILE_SIZE) {
            Utils.showMessage(view.context, R.string.stream_large_size_file_error)
        } else {
            if (!selectedAttachments.contains(attachment)) {
                attachment.isSelected = true
                selectedAttachments = selectedAttachments + attachment
                showSelectedAttachments(isMedia)
                rootController.configSendButtonEnableState()
                addAttachmentToAdapter(attachment, messageInputType, isMedia)
            }
        }
    }

    private fun checkPermissions() {
        when {
            PermissionChecker.isGrantedCameraPermissions(view.context) -> {
                view.showMediaPermissions(false)
                view.showCameraPermissions(false)
                view.showFilePermissions(true)
            }
            PermissionChecker.isGrantedStoragePermissions(view.context) -> {
                view.showMediaPermissions(false)
                view.showCameraPermissions(true)
                view.showFilePermissions(true)
            }
            else -> {
                view.showMediaPermissions(true)
                view.showCameraPermissions(true)
                view.showFilePermissions(true)
            }
        }
    }

    internal fun clearState() {
        view.hideAttachmentsMenu()
        selectedAttachments = emptySet()
        totalMediaAttachmentAdapter?.clear()
        totalFileAttachmentAdapter?.clear()
        selectedFileAttachmentAdapter?.clear()
        selectedMediaAttachmentAdapter?.clear()
    }

    fun onClickOpenAttachmentSelectionMenu() {
        view.showAttachmentsMenu()
        checkPermissions()
    }
}
