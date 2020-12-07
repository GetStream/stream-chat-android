package io.getstream.chat.android.ui.attachments

import android.content.Context
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.getstream.sdk.chat.model.AttachmentMetaData
import com.getstream.sdk.chat.model.ModelType
import com.getstream.sdk.chat.utils.StorageHelper
import io.getstream.chat.android.core.internal.coroutines.DispatcherProvider
import io.getstream.chat.android.ui.attachments.selected.SelectedFileAttachmentAdapter
import io.getstream.chat.android.ui.attachments.selected.SelectedMediaAttachmentAdapter
import io.getstream.chat.android.ui.utils.extensions.getFragmentManager
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

internal class AttachmentController(
    private val context: Context,
    private val selectedMediaRecyclerView: RecyclerView,
    private val selectedFilesRecyclerView: RecyclerView,
    private val storageHelper: StorageHelper = StorageHelper(),
    private val selectedFileAttachmentAdapter: SelectedFileAttachmentAdapter = SelectedFileAttachmentAdapter(),
    private val selectedMediaAttachmentAdapter: SelectedMediaAttachmentAdapter = SelectedMediaAttachmentAdapter(),
    private val selectedAttachmentsChangeListener: (List<AttachmentMetaData>) -> Unit
) {
    internal var selectedAttachments: List<AttachmentMetaData> = emptyList()
        private set

    private val attachmentSelectionListener = object : AttachmentSelectionListener {
        override fun onAttachmentsSelected(attachments: Set<AttachmentMetaData>, attachmentSource: AttachmentSource) {
            if (attachments.isNotEmpty()) {
                when (attachmentSource) {
                    AttachmentSource.MEDIA,
                    AttachmentSource.CAMERA -> onMediaAttachmentsSelected(attachments)
                    AttachmentSource.FILE -> onFileAttachmentsSelected(attachments)
                }
            }
        }
    }

    init {
        selectedFilesRecyclerView.itemAnimator = null
        selectedFileAttachmentAdapter.onAttachmentCancelled = ::cancelAttachment
        selectedFilesRecyclerView.adapter = selectedFileAttachmentAdapter
        selectedMediaAttachmentAdapter.onAttachmentCancelled = ::cancelAttachment
        selectedMediaRecyclerView.adapter = selectedMediaAttachmentAdapter
    }

    fun openAttachmentDialog() {
        context.getFragmentManager()?.let {
            AttachmentDialogFragment.newInstance()
                .apply { setAttachmentSelectionListener(attachmentSelectionListener) }
                .show(it, AttachmentDialogFragment.TAG)
        }
    }

    fun clearSelectedAttachments() {
        selectedAttachments = emptyList()
        selectedFilesRecyclerView.isVisible = false
        selectedFileAttachmentAdapter.clear()
        selectedMediaRecyclerView.isVisible = false
        selectedMediaAttachmentAdapter.clear()
    }

    fun getSelectedAttachmentsFiles(): List<File> = selectedAttachments.map {
        storageHelper.getCachedFileFromUri(context, it)
    }

    private fun cancelAttachment(attachment: AttachmentMetaData) {
        selectedAttachments = selectedAttachments - attachment
        selectedFileAttachmentAdapter.removeItem(attachment)
        selectedMediaAttachmentAdapter.removeItem(attachment)

        if (selectedAttachments.isEmpty()) {
            clearSelectedAttachments()
        }

        selectedAttachmentsChangeListener(selectedAttachments)
    }

    private fun onMediaAttachmentsSelected(attachments: Set<AttachmentMetaData>) {
        selectedAttachments = attachments
            .filter { it.uri != null }
            .filter { it.type in MEDIA_ATTACHMENT_TYPES }

        selectedFilesRecyclerView.isVisible = false
        selectedFileAttachmentAdapter.clear()
        selectedMediaRecyclerView.isVisible = true
        selectedMediaAttachmentAdapter.setItems(selectedAttachments)

        selectedAttachmentsChangeListener(selectedAttachments)
    }

    private fun onFileAttachmentsSelected(attachments: Set<AttachmentMetaData>) {
        GlobalScope.launch(DispatcherProvider.Main) {
            val uris = attachments.mapNotNull(AttachmentMetaData::uri)

            selectedAttachments = withContext(DispatcherProvider.IO) {
                storageHelper.getAttachmentsFromUriList(context, uris).toMutableList()
            }

            selectedMediaRecyclerView.isVisible = false
            selectedMediaAttachmentAdapter.clear()
            selectedFilesRecyclerView.isVisible = true
            selectedFileAttachmentAdapter.setItems(selectedAttachments)

            selectedAttachmentsChangeListener(selectedAttachments)
        }
    }

    companion object {
        private val MEDIA_ATTACHMENT_TYPES = listOf(ModelType.attach_image, ModelType.attach_video)
    }
}
