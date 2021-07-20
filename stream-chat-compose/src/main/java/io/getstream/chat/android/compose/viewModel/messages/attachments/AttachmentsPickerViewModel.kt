package io.getstream.chat.android.compose.viewModel.messages.attachments

import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.getstream.sdk.chat.model.AttachmentMetaData
import io.getstream.chat.android.client.models.Attachment
import io.getstream.chat.android.compose.state.messages.attachments.*
import io.getstream.chat.android.compose.ui.util.StorageHelperWrapper
import io.getstream.chat.android.core.internal.InternalStreamChatApi
import io.getstream.chat.android.offline.ChatDomain

/**
 * ViewModel responsible for handling the state and business logic of attachments.
 *
 * Used to load file and media images that are then connected to the UI. It also keeps state of the
 * selected items and prepares items before sending them.
 * */
@InternalStreamChatApi
class AttachmentsPickerViewModel constructor(
    private val chatDomain: ChatDomain,
    private val storageHelper: StorageHelperWrapper
) : ViewModel() {

    /**
     * Currently selected picker mode. [Images], [Files] or [MediaCapture].
     * */
    var attachmentsPickerMode: AttachmentsPickerMode by mutableStateOf(Images)
        private set

    /**
     * List of images available, from the system.
     * */
    var images: List<AttachmentItem> by mutableStateOf(emptyList())

    /**
     * List of files available, from the system.
     * */
    var files: List<AttachmentItem> by mutableStateOf(emptyList())

    /**
     * Gives us info if there are any file items that are selected.
     * */
    val hasPickedFiles: Boolean
        get() = files.any { it.isSelected }

    /**
     * Gives us info if there are any image items that are selected.
     * */
    val hasPickedImages: Boolean
        get() = images.any { it.isSelected }

    /**
     * Gives us information if we're showing the attachments picker or not.
     * */
    var isShowingAttachments: Boolean by mutableStateOf(false)
        private set

    /**
     * Starts the ViewModel, by loading all the items based on the current type.
     * */
    fun start() {
        loadAttachmentsData(attachmentsPickerMode)
    }

    /**
     * Changes the currently selected [AttachmentsPickerMode] and loads the required data.
     *
     * @param attachmentsPickerMode - The currently selected picker mode.
     * */
    fun onAttachmentsModeSelected(attachmentsPickerMode: AttachmentsPickerMode) {
        this.attachmentsPickerMode = attachmentsPickerMode

        loadAttachmentsData(attachmentsPickerMode)
    }

    /**
     * Notifies the ViewModel if we should show attachments or not.
     *
     * @param showAttachments - If we need to show attachments or hide them.
     * */
    fun onShowAttachments(showAttachments: Boolean) {
        isShowingAttachments = showAttachments

        if (!showAttachments) {
            attachmentsPickerMode = Images
        }
    }

    /**
     * Loads the attachment data, based on the [AttachmentsPickerMode].
     *
     * @param attachmentsPickerMode - The currently selected picker mode.
     * */
    private fun loadAttachmentsData(attachmentsPickerMode: AttachmentsPickerMode) {
        if (attachmentsPickerMode == Images) {
            images = storageHelper.getMedia().map { AttachmentItem(it, false) }
            files = emptyList()
        } else if (attachmentsPickerMode == Files) {
            files = storageHelper.getFiles().map { AttachmentItem(it, false) }
            images = emptyList()
        }
    }

    /**
     * Triggered when an [AttachmentMetaData] is selected in the list. Added or removed from the
     * corresponding list, be it [files] or [images], based on [attachmentsPickerMode].
     *
     * @param attachmentItem - The selected item.
     * */
    fun onAttachmentSelected(attachmentItem: AttachmentItem) {
        val dataSet = if (attachmentsPickerMode == Files) files else images

        val itemIndex = dataSet.indexOf(attachmentItem)
        val newFiles = dataSet.toMutableList()

        val newItem = dataSet[itemIndex].copy(isSelected = !newFiles[itemIndex].isSelected)

        newFiles.removeAt(itemIndex)
        newFiles.add(itemIndex, newItem)

        if (attachmentsPickerMode == Files) {
            files = newFiles
        } else {
            images = newFiles
        }
    }

    /**
     * Loads up the currently selected attachments. It uses the [attachmentsPickerMode] to know which
     * attachments to use - files or images.
     *
     * It maps all files to a list of [Attachment] objects, based on their type.
     * */
    fun getSelectedAttachments(): List<Attachment> {
        val dataSet = if (attachmentsPickerMode == Files) files else images
        val selectedAttachments = dataSet.filter { it.isSelected }

        return storageHelper.getAttachmentsForUpload(selectedAttachments.map { it.attachmentMetaData })
    }

    /**
     * Transforms selected file Uris to a list of [Attachment]s we can upload.
     *
     * @param uris - Selected Uris.
     * @return - List of [Attachment]s ready for uploading.
     * */
    fun getAttachmentsFromUris(uris: List<Uri>): List<Attachment> {
        return storageHelper.getAttachmentsFromUris(uris)
    }

    /**
     * Transforms the selected meta data into a list of [Attachment]s we can upload.
     *
     * @param metaData - List of attachment meta data items.
     * @return - List of [Attachment]s, ready for uploading.
     * */
    fun getAttachmentsFromMetaData(metaData: List<AttachmentMetaData>): List<Attachment> {
        return storageHelper.getAttachmentsForUpload(metaData)
    }

    /**
     * Triggered when we dismiss the attachments picker. We reset the state to show images and clear
     * the items for now, until the user needs them again.
     * */
    fun onDismiss() {
        attachmentsPickerMode = Images
        images = emptyList()
        files = emptyList()
    }
}