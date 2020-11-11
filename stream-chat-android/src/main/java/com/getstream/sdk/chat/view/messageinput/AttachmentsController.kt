package com.getstream.sdk.chat.view.messageinput

import android.net.Uri
import androidx.annotation.VisibleForTesting
import com.getstream.sdk.chat.R
import com.getstream.sdk.chat.adapter.FileAttachmentSelectedAdapter
import com.getstream.sdk.chat.adapter.MediaAttachmentAdapter
import com.getstream.sdk.chat.adapter.MediaAttachmentSelectedAdapter
import com.getstream.sdk.chat.enums.MessageInputType
import com.getstream.sdk.chat.model.AttachmentMetaData
import com.getstream.sdk.chat.model.ModelType
import com.getstream.sdk.chat.utils.Constant
import com.getstream.sdk.chat.utils.PermissionChecker
import com.getstream.sdk.chat.utils.StorageHelper
import io.getstream.chat.android.client.internal.DispatcherProvider
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

internal class AttachmentsController(
    private val rootController: MessageInputController,
    private val permissionChecker: PermissionChecker,
    private val storageHelper: StorageHelper,
    private val view: MessageInputView,
    private val totalMediaAttachmentAdapter: MediaAttachmentAdapter,
    private val selectedMediaAttachmentAdapter: MediaAttachmentSelectedAdapter,
    private val selectedFileAttachmentAdapter: FileAttachmentSelectedAdapter,
    private val showOpenAttachmentsMenuConfig: Boolean
) {
    internal var selectedAttachments: Set<AttachmentMetaData> = emptySet()
        private set
    private var totalAttachments: Set<AttachmentMetaData> = emptySet()

    internal fun setSelectedAttachments(selectedAttachments: Set<AttachmentMetaData>) {
        this.selectedAttachments += selectedAttachments
    }

    internal fun onClickCloseAttachmentSelectionMenu() {
        view.hideAttachmentsMenu()
        totalAttachments = emptySet()
        totalMediaAttachmentAdapter.clear()
        configAttachmentButtonVisible(true)
    }

    private fun fillTotalMediaAttachmentsView(messageInputType: MessageInputType?) {
        GlobalScope.launch(DispatcherProvider.Main) {
            view.showLoadingTotalAttachments(true)
            totalAttachments = withContext(DispatcherProvider.IO) {
                storageHelper.getMediaAttachments(view.context).toSet()
            }
            if (totalAttachments.isEmpty()) {
                view.showMessage(R.string.stream_no_media_error)
                onClickCloseAttachmentSelectionMenu()
            } else {
                setTotalMediaAttachmentAdapter(
                    totalAttachments.toList(),
                    selectedAttachments.toList(),
                    messageInputType
                )
            }
            setSelectedMediaAttachmentAdapter(messageInputType)
            view.showLoadingTotalAttachments(false)
        }
    }

    internal fun configAttachmentButtonVisible(visible: Boolean) {
        if (!showOpenAttachmentsMenuConfig) return
        view.showOpenAttachmentsMenuButton(visible)
    }

    @VisibleForTesting
    internal fun cancelAttachment(
        attachment: AttachmentMetaData,
        messageInputType: MessageInputType?
    ) {
        selectedAttachments = selectedAttachments - attachment
        removeAttachmentFromAdapters(attachment)
        rootController.configSendButtonEnableState()
        if (selectedAttachments.isEmpty() && MessageInputType.EDIT_MESSAGE == messageInputType) {
            configAttachmentButtonVisible(true)
        }
    }

    internal fun onCameraClick() {
        if (!permissionChecker.isGrantedCameraPermissions(view.context)) {
            permissionChecker.checkCameraPermissions(view) { onCameraClick() }
        } else {
            view.showCameraOptions()
        }
    }

    internal fun onClickOpenMediaSelectView(messageInputType: MessageInputType) {
        if (!permissionChecker.isGrantedStoragePermissions(view.context)) {
            permissionChecker.checkStoragePermissions(view) {
                onClickOpenMediaSelectView(messageInputType)
            }
            return
        }

        setSelectedAttachments(selectedAttachments)
        fillTotalMediaAttachmentsView(messageInputType)
    }

    private fun removeAttachmentFromAdapters(attachment: AttachmentMetaData) {
        if (isMediaAttachment(attachment)) {
            selectedMediaAttachmentAdapter.removeAttachment(attachment)
            totalMediaAttachmentAdapter.unselectAttachment(attachment)
        } else {
            selectedFileAttachmentAdapter.setAttachments(selectedAttachments.toList())
        }
    }

    private fun setTotalMediaAttachmentAdapter(
        totalAttachment: List<AttachmentMetaData>,
        selectedAttachments: List<AttachmentMetaData>,
        messageInputType: MessageInputType?
    ) {
        totalAttachment.forEach { it.isSelected = selectedAttachments.contains(it) }
        totalMediaAttachmentAdapter.apply {
            setAttachments(totalAttachment)
            listener = { attachment -> updateMediaAttachment(attachment, messageInputType) }
        }
        view.showTotalMediaAttachments(totalMediaAttachmentAdapter)
    }

    private fun setSelectedMediaAttachmentAdapter(messageInputType: MessageInputType?) {
        selectedAttachments = selectedAttachments.filter(isMediaAttachment).toSet()
        selectedMediaAttachmentAdapter.setAttachments(selectedAttachments.toList())
        selectedMediaAttachmentAdapter.cancelListener =
            { attachment -> cancelAttachment(attachment, messageInputType) }
        view.showSelectedMediaAttachments(selectedMediaAttachmentAdapter)
        selectedFileAttachmentAdapter.clear()
    }

    private fun setSelectedFileAttachmentAdapter() {
        selectedAttachments = selectedAttachments.filter(isFileAttachment).toSet()
        selectedFileAttachmentAdapter.cancelListener =
            { attachment -> cancelAttachment(attachment, null) }
        view.showSelectedFileAttachments(selectedFileAttachmentAdapter)
        selectedMediaAttachmentAdapter.clear()
    }

    private fun updateMediaAttachment(
        attachment: AttachmentMetaData,
        messageInputType: MessageInputType?
    ) = when (attachment.isSelected) {
        true -> unselectMediaAttachment(attachment, messageInputType)
        false -> selectMediaAttachment(attachment)
    }

    private fun unselectMediaAttachment(
        attachment: AttachmentMetaData,
        messageInputType: MessageInputType?
    ) {
        attachment.isSelected = false
        selectedAttachments = selectedAttachments - attachment
        removeAttachmentFromAdapters(attachment)
        rootController.configSendButtonEnableState()
        if (selectedAttachments.isEmpty() && MessageInputType.EDIT_MESSAGE == messageInputType) {
            configAttachmentButtonVisible(true)
        }
    }

    internal fun selectAttachmentFromCamera(attachment: AttachmentMetaData) {
        setSelectedMediaAttachmentAdapter(null)
        selectMediaAttachment(attachment)
    }

    internal fun selectAttachmentsFromUriList(uriList: List<Uri>) {
        GlobalScope.launch(DispatcherProvider.Main) {
            setSelectedFileAttachmentAdapter()
            val attachments = withContext(DispatcherProvider.IO) {
                storageHelper.getAttachmentsFromUriList(view.context, uriList)
            }
            selectFileAttachments(attachments)
        }
    }

    private fun selectFileAttachments(attachments: List<AttachmentMetaData>) {
        val filteredAttachments = attachments.filter { it.size <= Constant.MAX_UPLOAD_FILE_SIZE }
        if (filteredAttachments.size < attachments.size) {
            view.showMessage(R.string.stream_large_size_file_error)
        }
        filteredAttachments.forEach { it.isSelected = true }
        selectedAttachments = selectedAttachments + filteredAttachments
        view.showFileAttachments()
        rootController.configSendButtonEnableState()
        selectedFileAttachmentAdapter.setAttachments(selectedAttachments.toList())
    }

    internal fun selectMediaAttachment(attachment: AttachmentMetaData) {
        if (attachment.size > Constant.MAX_UPLOAD_FILE_SIZE) {
            view.showMessage(R.string.stream_large_size_file_error)
        } else {
            if (!selectedAttachments.contains(attachment)) {
                attachment.isSelected = true
                selectedAttachments = selectedAttachments + attachment
                view.showMediaAttachments()
                rootController.configSendButtonEnableState()
                selectedMediaAttachmentAdapter.addAttachment(attachment)
                totalMediaAttachmentAdapter.selectAttachment(attachment)
            }
        }
    }

    private fun checkPermissions() {
        when {
            permissionChecker.isGrantedCameraPermissions(view.context) -> {
                view.showMediaPermissions(false)
                view.showCameraPermissions(false)
            }
            permissionChecker.isGrantedStoragePermissions(view.context) -> {
                view.showMediaPermissions(false)
                view.showCameraPermissions(true)
            }
            else -> {
                view.showMediaPermissions(true)
                view.showCameraPermissions(true)
            }
        }
    }

    internal fun clearState() {
        view.hideAttachmentsMenu()
        selectedAttachments = emptySet()
        totalMediaAttachmentAdapter.clear()
        selectedFileAttachmentAdapter.clear()
        selectedMediaAttachmentAdapter.clear()
    }

    fun onClickOpenAttachmentSelectionMenu() {
        view.showAttachmentsMenu()
        checkPermissions()
    }

    companion object {
        private val listOfMediaTypes = listOf(ModelType.attach_image, ModelType.attach_video)
        private val isMediaAttachment: (AttachmentMetaData) -> Boolean =
            { it.type in listOfMediaTypes }
        private val isFileAttachment: (AttachmentMetaData) -> Boolean =
            { it.type == ModelType.attach_file }
    }
}
