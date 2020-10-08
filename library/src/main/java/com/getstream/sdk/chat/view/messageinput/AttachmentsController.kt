package com.getstream.sdk.chat.view.messageinput

import android.net.Uri
import androidx.annotation.VisibleForTesting
import com.getstream.sdk.chat.R
import com.getstream.sdk.chat.adapter.FileAttachmentSelectedAdapter
import com.getstream.sdk.chat.adapter.MediaAttachmentAdapter
import com.getstream.sdk.chat.adapter.MediaAttachmentSelectedAdapter
import com.getstream.sdk.chat.enums.MessageInputType
import com.getstream.sdk.chat.infrastructure.DispatchersProvider
import com.getstream.sdk.chat.model.AttachmentMetaData
import com.getstream.sdk.chat.model.ModelType
import com.getstream.sdk.chat.utils.Constant
import com.getstream.sdk.chat.utils.PermissionChecker
import com.getstream.sdk.chat.utils.StorageHelper
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

internal class AttachmentsController(
    private val rootController: MessageInputController,
    private val permissionChecker: PermissionChecker,
    private val storageHelper: StorageHelper,
    private val dispatchersProvider: DispatchersProvider,
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
        GlobalScope.launch(dispatchersProvider.mainDispatcher) {
            view.showLoadingTotalAttachments(true)
            totalAttachments = withContext(dispatchersProvider.ioDispatcher) {
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
            setSelectedAttachmentAdapter(messageInputType, true)
            view.showLoadingTotalAttachments(false)
        }
    }

    private fun filterAttachments(
        isMedia: Boolean,
        filesToFilter: Set<AttachmentMetaData>
    ): Set<AttachmentMetaData> {
        return filesToFilter.filter(if (isMedia) isMediaAttachment else isFileAttachment)
            .toSet()
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

    private fun showSelectedAttachments(isMedia: Boolean) {
        if (isMedia) {
            view.showMediaAttachments()
        } else {
            view.showFileAttachments()
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

    private fun addAttachmentToAdapter(attachment: AttachmentMetaData) {
        if (isMediaAttachment(attachment)) {
            selectedMediaAttachmentAdapter.addAttachment(attachment)
            totalMediaAttachmentAdapter.selectAttachment(attachment)
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

    internal fun setSelectedAttachmentAdapter(
        messageInputType: MessageInputType?,
        isMedia: Boolean
    ) {
        selectedAttachments = filterAttachments(isMedia, selectedAttachments)
        if (isMedia) {
            selectedMediaAttachmentAdapter.setAttachments(selectedAttachments.toList())
            selectedMediaAttachmentAdapter.cancelListener =
                { attachment -> cancelAttachment(attachment, messageInputType) }
            view.showSelectedMediaAttachments(selectedMediaAttachmentAdapter)
            selectedFileAttachmentAdapter.clear()
        } else {
            selectedFileAttachmentAdapter.setAttachments(selectedAttachments.toList())
            selectedFileAttachmentAdapter.cancelListener =
                { attachment -> cancelAttachment(attachment, messageInputType) }
            view.showSelectedFileAttachments(selectedFileAttachmentAdapter)
            selectedMediaAttachmentAdapter.clear()
        }
    }

    private fun updateMediaAttachment(
        attachment: AttachmentMetaData,
        messageInputType: MessageInputType?
    ) = when (attachment.isSelected) {
        true -> unselectMediaAttachment(attachment, messageInputType)
        false -> selectAttachment(attachment)
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

    internal fun selectAttachmentsFromUriList(uriList: List<Uri>) {
        GlobalScope.launch(dispatchersProvider.mainDispatcher) {
            setSelectedAttachmentAdapter(null, false)
            val attachments = withContext(dispatchersProvider.ioDispatcher) {
                storageHelper.getAttachmentsFromUriList(view.context, uriList)
            }
            attachments.forEach { selectAttachment(it) }
        }
    }

    internal fun selectAttachment(attachment: AttachmentMetaData) {
        if (attachment.size > Constant.MAX_UPLOAD_FILE_SIZE) {
            view.showMessage(R.string.stream_large_size_file_error)
        } else {
            if (!selectedAttachments.contains(attachment)) {
                attachment.isSelected = true
                selectedAttachments = selectedAttachments + attachment
                showSelectedAttachments(isMediaAttachment(attachment))
                rootController.configSendButtonEnableState()
                addAttachmentToAdapter(attachment)
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
