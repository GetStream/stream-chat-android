package io.getstream.chat.android.compose.viewmodel.messages

import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.getstream.sdk.chat.model.AttachmentMetaData
import io.getstream.chat.android.client.models.Attachment
import io.getstream.chat.android.compose.state.messages.attachments.AttachmentItem
import io.getstream.chat.android.compose.state.messages.attachments.AttachmentsPickerMode
import io.getstream.chat.android.compose.state.messages.attachments.Files
import io.getstream.chat.android.compose.state.messages.attachments.Images
import io.getstream.chat.android.compose.state.messages.attachments.MediaCapture
import io.getstream.chat.android.compose.ui.util.StorageHelperWrapper

/**
 * ViewModel responsible for handling the state and business logic of attachments.
 *
 * Used to load file and media images that are then connected to the UI. It also keeps state of the
 * selected items and prepares items before sending them.
 * */
public class AttachmentsPickerViewModel(
    private val storageHelper: StorageHelperWrapper,
) : ViewModel() {

    /**
     * Currently selected picker mode. [Images], [Files] or [MediaCapture].
     * */
    public var attachmentsPickerMode: AttachmentsPickerMode by mutableStateOf(Images)
        private set

    /**
     * List of images available, from the system.
     * */
    public var images: List<AttachmentItem> by mutableStateOf(emptyList())

    /**
     * List of files available, from the system.
     * */
    public var files: List<AttachmentItem> by mutableStateOf(emptyList())

    /**
     * Gives us info if there are any file items that are selected.
     * */
    public val hasPickedFiles: Boolean
        get() = files.any { it.isSelected }

    /**
     * Gives us info if there are any image items that are selected.
     * */
    public val hasPickedImages: Boolean
        get() = images.any { it.isSelected }

    /**
     * Gives us information if we're showing the attachments picker or not.
     * */
    public var isShowingAttachments: Boolean by mutableStateOf(false)
        private set

    init {
        loadData()
    }

    /**
     * Loads all the items based on the current type.
     * */
    public fun loadData() {
        loadAttachmentsData(attachmentsPickerMode)
    }

    /**
     * Changes the currently selected [AttachmentsPickerMode] and loads the required data.
     *
     * @param attachmentsPickerMode - The currently selected picker mode.
     * */
    public fun changeAttachmentPickerMode(attachmentsPickerMode: AttachmentsPickerMode) {
        this.attachmentsPickerMode = attachmentsPickerMode

        loadAttachmentsData(attachmentsPickerMode)
    }

    /**
     * Notifies the ViewModel if we should show attachments or not.
     *
     * @param showAttachments - If we need to show attachments or hide them.
     * */
    public fun changeAttachmentState(showAttachments: Boolean) {
        isShowingAttachments = showAttachments

        if (!showAttachments) {
            dismissAttachments()
        } else {
            loadData()
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
    public fun changeSelectedAttachments(attachmentItem: AttachmentItem) {
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
    public fun getSelectedAttachments(): List<Attachment> {
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
    public fun getAttachmentsFromUris(uris: List<Uri>): List<Attachment> {
        return storageHelper.getAttachmentsFromUris(uris)
    }

    /**
     * Transforms the selected meta data into a list of [Attachment]s we can upload.
     *
     * @param metaData - List of attachment meta data items.
     * @return - List of [Attachment]s, ready for uploading.
     * */
    public fun getAttachmentsFromMetaData(metaData: List<AttachmentMetaData>): List<Attachment> {
        return storageHelper.getAttachmentsForUpload(metaData)
    }

    /**
     * Triggered when we dismiss the attachments picker. We reset the state to show images and clear
     * the items for now, until the user needs them again.
     * */
    public fun dismissAttachments() {
        attachmentsPickerMode = Images
        images = emptyList()
        files = emptyList()
    }
}
