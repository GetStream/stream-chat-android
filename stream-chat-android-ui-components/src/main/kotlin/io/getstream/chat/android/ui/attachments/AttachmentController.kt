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

internal class AttachmentController(
    private val context: Context,
    private val selectedMediaRecyclerView: RecyclerView,
    private val selectedFilesRecyclerView: RecyclerView,
    private val selectedAttachmentsChangeListener: (List<AttachmentMetaData>) -> Unit

) {
    internal var selectedAttachments: List<AttachmentMetaData> = emptyList()
        private set

    private val storageHelper = StorageHelper()
    private val selectedFileAttachmentAdapter = SelectedFileAttachmentAdapter(::cancelAttachment)
    private val selectedMediaAttachmentAdapter = SelectedMediaAttachmentAdapter(::cancelAttachment)

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
        selectedFilesRecyclerView.adapter = selectedFileAttachmentAdapter
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

    private fun cancelAttachment(attachment: AttachmentMetaData) {
        selectedAttachments = selectedAttachments - attachment
        selectedFileAttachmentAdapter.removeAttachment(attachment)
        selectedMediaAttachmentAdapter.removeAttachment(attachment)

        if (selectedAttachments.isEmpty()) {
            selectedFilesRecyclerView.isVisible = false
            selectedFileAttachmentAdapter.clear()
            selectedMediaRecyclerView.isVisible = false
            selectedMediaAttachmentAdapter.clear()
        }

        selectedAttachmentsChangeListener(selectedAttachments)
    }

    private fun onMediaAttachmentsSelected(attachments: Set<AttachmentMetaData>) {
        selectedAttachments = attachments
            .filter { it.uri != null }
            .filter { it.type in listOf(ModelType.attach_image, ModelType.attach_video) }

        selectedFilesRecyclerView.isVisible = false
        selectedFileAttachmentAdapter.clear()
        selectedMediaRecyclerView.isVisible = true
        selectedMediaAttachmentAdapter.setAttachments(selectedAttachments)

        selectedAttachmentsChangeListener(selectedAttachments)
    }

    private fun onFileAttachmentsSelected(attachments: Set<AttachmentMetaData>) {
        GlobalScope.launch(DispatcherProvider.Main) {
            val uris = attachments
                .mapNotNull { it.uri }

            selectedAttachments = withContext(DispatcherProvider.IO) {
                storageHelper.getAttachmentsFromUriList(context, uris).toMutableList()
            }

            selectedMediaRecyclerView.isVisible = false
            selectedMediaAttachmentAdapter.clear()
            selectedFilesRecyclerView.isVisible = true
            selectedFileAttachmentAdapter.setAttachments(selectedAttachments)

            selectedAttachmentsChangeListener(selectedAttachments)
        }
    }
}
