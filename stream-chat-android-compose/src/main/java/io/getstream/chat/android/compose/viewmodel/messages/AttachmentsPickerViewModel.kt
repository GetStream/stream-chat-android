/*
 * Copyright (c) 2014-2022 Stream.io Inc. All rights reserved.
 *
 * Licensed under the Stream License;
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    https://github.com/GetStream/stream-chat-android/blob/main/LICENSE
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.getstream.chat.android.compose.viewmodel.messages

import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import io.getstream.chat.android.compose.state.messages.attachments.AttachmentPickerItemState
import io.getstream.chat.android.compose.state.messages.attachments.AttachmentsPickerMode
import io.getstream.chat.android.compose.state.messages.attachments.Files
import io.getstream.chat.android.compose.state.messages.attachments.Images
import io.getstream.chat.android.compose.state.messages.attachments.MediaCapture
import io.getstream.chat.android.compose.ui.util.StorageHelperWrapper
import io.getstream.chat.android.models.Attachment
import io.getstream.chat.android.ui.common.state.messages.composer.AttachmentMetaData

/**
 * ViewModel responsible for handling the state and business logic of attachments.
 *
 * Used to load file and media images that are then connected to the UI. It also keeps state of the
 * selected items and prepares items before sending them.
 */
public class AttachmentsPickerViewModel(
    private val storageHelper: StorageHelperWrapper,
) : ViewModel() {

    /**
     * Currently selected picker mode. [Images], [Files] or [MediaCapture].
     */
    public var attachmentsPickerMode: AttachmentsPickerMode by mutableStateOf(Images)
        private set

    /**
     * List of images available, from the system.
     */
    public var images: List<AttachmentPickerItemState> by mutableStateOf(emptyList())

    /**
     * List of files available, from the system.
     */
    public var files: List<AttachmentPickerItemState> by mutableStateOf(emptyList())

    /**
     * List of attachments available, from the system.
     */
    public var attachments: List<AttachmentPickerItemState> by mutableStateOf(emptyList())

    /**
     * Gives us info if there are any file items that are selected.
     */
    public val hasPickedFiles: Boolean
        get() = files.any { it.isSelected }

    /**
     * Gives us info if there are any image items that are selected.
     */
    public val hasPickedImages: Boolean
        get() = images.any { it.isSelected }

    /**
     * Gives us info if there are any attachment items that are selected.
     */
    public val hasPickedAttachments: Boolean
        get() = attachments.any { it.isSelected }

    /**
     * Gives us information if we're showing the attachments picker or not.
     */
    public var isShowingAttachments: Boolean by mutableStateOf(false)
        private set

    /**
     * Loads all the items based on the current type.
     */
    public fun loadData() {
        loadAttachmentsData(attachmentsPickerMode)
    }

    /**
     * Changes the currently selected [AttachmentsPickerMode] and loads the required data. If no permission is granted
     * will not try and load data to avoid crashes.
     *
     * @param attachmentsPickerMode The currently selected picker mode.
     * @param hasPermission Handler to check if there is permission for wanted action.
     */
    public fun changeAttachmentPickerMode(
        attachmentsPickerMode: AttachmentsPickerMode,
        hasPermission: () -> Boolean = { true },
    ) {
        this.attachmentsPickerMode = attachmentsPickerMode

        if (hasPermission()) loadAttachmentsData(attachmentsPickerMode)
    }

    /**
     * Notifies the ViewModel if we should show attachments or not.
     *
     * @param showAttachments If we need to show attachments or hide them.
     */
    public fun changeAttachmentState(showAttachments: Boolean) {
        isShowingAttachments = showAttachments

        if (!showAttachments) {
            dismissAttachments()
        }
    }

    /**
     * Loads the attachment data, based on the [AttachmentsPickerMode].
     *
     * @param attachmentsPickerMode The currently selected picker mode.
     */
    private fun loadAttachmentsData(attachmentsPickerMode: AttachmentsPickerMode) {
        if (attachmentsPickerMode == Images) {
            val images = storageHelper.getMedia().map { AttachmentPickerItemState(it, false) }
            this.images = images
            this.attachments = images
            this.files = emptyList()
        } else if (attachmentsPickerMode == Files) {
            val files = storageHelper.getFiles().map { AttachmentPickerItemState(it, false) }
            this.files = files
            this.attachments = files
            this.images = emptyList()
        }
    }

    /**
     * Triggered when an [AttachmentMetaData] is selected in the list. Added or removed from the
     * corresponding list, be it [files] or [images], based on [attachmentsPickerMode].
     *
     * @param attachmentItem The selected item.
     */
    public fun changeSelectedAttachments(attachmentItem: AttachmentPickerItemState) {
        val dataSet = attachments

        val itemIndex = dataSet.indexOf(attachmentItem)
        val newFiles = dataSet.toMutableList()

        val newItem = dataSet[itemIndex].copy(isSelected = !newFiles[itemIndex].isSelected)

        newFiles.removeAt(itemIndex)
        newFiles.add(itemIndex, newItem)

        if (attachmentsPickerMode == Files) {
            files = newFiles
        } else if (attachmentsPickerMode == Images) {
            images = newFiles
        }
        attachments = newFiles
    }

    /**
     * Loads up the currently selected attachments. It uses the [attachmentsPickerMode] to know which
     * attachments to use - files or images.
     *
     * It maps all files to a list of [Attachment] objects, based on their type.
     */
    public fun getSelectedAttachments(): List<Attachment> {
        val dataSet = if (attachmentsPickerMode == Files) files else images
        val selectedAttachments = dataSet.filter { it.isSelected }

        return storageHelper.getAttachmentsForUpload(selectedAttachments.map { it.attachmentMetaData })
    }

    /**
     * Transforms selected file Uris to a list of [Attachment]s we can upload.
     *
     * @param uris Selected Uris.
     * @return List of [Attachment]s ready for uploading.
     */
    public fun getAttachmentsFromUris(uris: List<Uri>): List<Attachment> {
        return storageHelper.getAttachmentsFromUris(uris)
    }

    /**
     * Transforms the selected meta data into a list of [Attachment]s we can upload.
     *
     * @param metaData List of attachment meta data items.
     * @return List of [Attachment]s, ready for uploading.
     */
    public fun getAttachmentsFromMetaData(metaData: List<AttachmentMetaData>): List<Attachment> {
        return storageHelper.getAttachmentsForUpload(metaData)
    }

    /**
     * Triggered when we dismiss the attachments picker. We reset the state to show images and clear
     * the items for now, until the user needs them again.
     */
    public fun dismissAttachments() {
        attachmentsPickerMode = Images
        images = emptyList()
        files = emptyList()
    }
}
