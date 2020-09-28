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
import com.getstream.sdk.chat.model.AttachmentMetaData
import com.getstream.sdk.chat.utils.Constant
import com.getstream.sdk.chat.utils.GridSpacingItemDecoration
import com.getstream.sdk.chat.utils.PermissionChecker
import com.getstream.sdk.chat.utils.StorageUtils
import com.getstream.sdk.chat.utils.Utils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

internal class AttachmentsController(
    private val rootController: MessageInputController,
    private val binding: StreamViewMessageInputBinding,
    private val view: MessageInputView,
    private val style: MessageInputStyle
) {

    private val gridLayoutManager = GridLayoutManager(view.context, 4, RecyclerView.VERTICAL, false)
    private val gridSpacingItemDecoration = GridSpacingItemDecoration(4, 2, false)

    private var totalMediaAttachmentAdapter: MediaAttachmentAdapter? = null
    private var selectedMediaAttachmentAdapter: MediaAttachmentSelectedAdapter? = null
    private var totalFileAttachmentAdapter: FileAttachmentListAdapter? = null
    private var selectedFileAttachmentAdapter: AttachmentListAdapter? = null

    internal var selectedAttachments: Set<AttachmentMetaData> = emptySet()
        private set
    private var totalAttachments: Set<AttachmentMetaData> = emptySet()

    init {
        binding.rvMedia.layoutManager = gridLayoutManager
        binding.rvMedia.addItemDecoration(gridSpacingItemDecoration)
    }

    internal fun setSelectedAttachments(selectedAttachments: Set<AttachmentMetaData>) {
        this.selectedAttachments += selectedAttachments
    }

    internal fun onClickCloseBackGroundView() {
        binding.clTitle.visibility = View.GONE
        binding.clAddFile.visibility = View.GONE
        binding.clSelectPhoto.visibility = View.GONE
        binding.root.setBackgroundResource(0)
        totalAttachments = emptySet()
        configAttachmentButtonVisible(true)
    }

    private fun configSelectAttachView(messageInputType: MessageInputType?, isMedia: Boolean, treeUri: Uri? = null) {
        GlobalScope.launch(Dispatchers.Main) {
            binding.progressBarFileLoader.visibility = View.VISIBLE
            totalAttachments = getAttachmentsFromLocal(isMedia, treeUri)
            if (selectedAttachments.isEmpty()) {
                setTotalAttachmentAdapters(totalAttachments.toList(), messageInputType, isMedia)
                if (totalAttachments.isEmpty()) {
                    Utils.showMessage(view.context, R.string.stream_no_media_error)
                    onClickCloseBackGroundView()
                }
            } else {
                showComposerAttachmentGalleryView(isMedia)
                setSelectedAttachmentAdapter(messageInputType, false, isMedia)
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

    internal fun configAttachmentButtonVisible(visible: Boolean) {
        if (!style.isShowAttachmentButton) return
        binding.ivOpenAttach.visibility = if (visible) View.VISIBLE else View.GONE
    }

    private fun cancelAttachment(
        attachment: AttachmentMetaData,
        messageInputType: MessageInputType?,
        fromGallery: Boolean,
        isMedia: Boolean
    ) {
        selectedAttachments = selectedAttachments - attachment
        if (fromGallery) totalAttachmentAdapterChanged(isMedia)
        removeAttachmentFromAdapters(attachment, messageInputType, fromGallery, isMedia)
        rootController.configSendButtonEnableState()
        if (selectedAttachments.isEmpty() && messageInputType == MessageInputType.EDIT_MESSAGE) configAttachmentButtonVisible(
            true
        )
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
        if (editAttachments.isNotEmpty()) {
            setSelectedAttachments(editAttachments)
        }
        configSelectAttachView(messageInputType, isMedia, treeUri)
    }

    private fun totalAttachmentAdapterChanged(isMedia: Boolean) {
        if (isMedia) totalMediaAttachmentAdapter?.notifyDataSetChanged() else totalFileAttachmentAdapter?.notifyDataSetChanged()
    }

    private fun removeAttachmentFromAdapters(
        attachment: AttachmentMetaData,
        messageInputType: MessageInputType?,
        fromGallery: Boolean,
        isMedia: Boolean
    ) {
        if (isMedia) {
            selectedMediaAttachmentAdapter?.removeAttachment(attachment) ?: setSelectedAttachmentAdapter(
                messageInputType,
                fromGallery,
                isMedia
            )
            totalMediaAttachmentAdapter?.unselectAttachment(attachment) ?: setTotalAttachmentAdapters(
                totalAttachments.toList(),
                messageInputType,
                isMedia
            )
        } else {
            if (selectedFileAttachmentAdapter == null) setSelectedAttachmentAdapter(
                messageInputType,
                fromGallery,
                isMedia
            )
            selectedFileAttachmentAdapter!!.notifyDataSetChanged()
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
                true,
                isMedia
            )
            totalMediaAttachmentAdapter?.selectAttachment(attachment) ?: setTotalAttachmentAdapters(
                totalAttachments.toList(),
                messageInputType,
                isMedia
            )
        } else {
            if (selectedFileAttachmentAdapter == null) setSelectedAttachmentAdapter(
                messageInputType,
                true,
                isMedia
            )
            selectedFileAttachmentAdapter!!.notifyDataSetChanged()
        }
    }

    private fun setTotalAttachmentAdapters(
        totalAttachment: List<AttachmentMetaData>,
        messageInputType: MessageInputType?,
        isMedia: Boolean
    ) {
        if (isMedia) {
            gridSpacingItemDecoration.setSpanCount(MEDIA_ITEMS_PER_ROW)
            gridLayoutManager.spanCount = MEDIA_ITEMS_PER_ROW
            totalMediaAttachmentAdapter =
                MediaAttachmentAdapter(totalAttachment) { updateAttachment(it, messageInputType, isMedia) }
            binding.rvMedia.adapter = totalMediaAttachmentAdapter
        } else {
            gridSpacingItemDecoration.setSpanCount(FILE_ITEMS_PER_ROW)
            gridLayoutManager.spanCount = FILE_ITEMS_PER_ROW
            totalFileAttachmentAdapter =
                FileAttachmentListAdapter(totalAttachment) { updateAttachment(it, messageInputType, isMedia) }
            binding.rvMedia.adapter = totalFileAttachmentAdapter
        }
    }

    private fun setSelectedAttachmentAdapter(
        messageInputType: MessageInputType?,
        fromGallery: Boolean,
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
                false
            ) { attachment: AttachmentMetaData ->
                cancelAttachment(
                    attachment,
                    messageInputType,
                    fromGallery,
                    isMedia
                )
            }
            binding.fileComposer.adapter = selectedFileAttachmentAdapter
            binding.fileComposer.visibility = View.VISIBLE
            binding.mediaComposer.visibility = View.GONE
            binding.mediaComposer.adapter = null
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
        removeAttachmentFromAdapters(attachment, messageInputType, true, isMedia)
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
            attachment.isSelected = true
            selectedAttachments = selectedAttachments + attachment
            showComposerAttachmentGalleryView(isMedia)
            rootController.configSendButtonEnableState()
            addAttachmentToAdapter(attachment, messageInputType, isMedia)
        }
    }

    internal fun checkPermissions() {
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

    internal fun clearState() {
        binding.fileComposer.visibility = View.GONE
        binding.mediaComposer.visibility = View.GONE
        selectedAttachments = emptySet()
        totalMediaAttachmentAdapter?.clear()
        totalFileAttachmentAdapter?.clear()
        selectedFileAttachmentAdapter?.clear()
        selectedMediaAttachmentAdapter?.clear()
    }

    companion object {
        private const val MEDIA_ITEMS_PER_ROW = 4
        private const val FILE_ITEMS_PER_ROW = 1
    }
}
