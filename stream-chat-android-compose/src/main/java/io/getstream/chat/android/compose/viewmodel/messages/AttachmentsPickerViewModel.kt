/*
 * Copyright (c) 2014-2026 Stream.io Inc. All rights reserved.
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
import androidx.annotation.VisibleForTesting
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.getstream.chat.android.client.channel.state.ChannelState
import io.getstream.chat.android.compose.state.messages.attachments.AttachmentPickerItemState
import io.getstream.chat.android.compose.state.messages.attachments.AttachmentPickerItemState.Selection
import io.getstream.chat.android.compose.state.messages.attachments.AttachmentsPickerMode
import io.getstream.chat.android.compose.state.messages.attachments.Files
import io.getstream.chat.android.compose.state.messages.attachments.Images
import io.getstream.chat.android.compose.state.messages.attachments.MediaCapture
import io.getstream.chat.android.compose.ui.util.StorageHelperWrapper
import io.getstream.chat.android.compose.util.extensions.asState
import io.getstream.chat.android.core.internal.coroutines.DispatcherProvider
import io.getstream.chat.android.models.Attachment
import io.getstream.chat.android.models.Channel
import io.getstream.chat.android.ui.common.state.messages.composer.AttachmentMetaData
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * ViewModel responsible for handling the state and business logic of attachments.
 *
 * Loads media or files based on the current picker mode, keeps selection state, and prepares items
 * for upload.
 */
public class AttachmentsPickerViewModel(
    private val storageHelper: StorageHelperWrapper,
    channelState: StateFlow<ChannelState?>,
) : ViewModel() {

    /**
     * The information for the current [Channel].
     */
    public val channel: Channel by channelState
        .filterNotNull()
        .map(ChannelState::toChannel)
        .asState(viewModelScope, Channel())

    /**
     * Currently selected picker mode. [Images], [Files] or [MediaCapture].
     */
    public var attachmentsPickerMode: AttachmentsPickerMode by mutableStateOf(Images)
        private set

    /**
     * List of attachments available for the current picker mode.
     */
    public var attachments: List<AttachmentPickerItemState> by mutableStateOf(emptyList())

    /**
     * List of polls available, from the system.
     */
    public var polls: List<AttachmentPickerItemState> by mutableStateOf(emptyList())

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
     * Changes the currently selected [AttachmentsPickerMode] and loads the required data.
     * If no permission is granted, it will not try to load data to avoid crashes.
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
     * Toggles the visibility of the attachments picker.
     */
    public fun toggleAttachmentState() {
        changeAttachmentState(showAttachments = !isShowingAttachments)
    }

    @VisibleForTesting
    internal fun loadAttachmentsData(attachmentsPickerMode: AttachmentsPickerMode) {
        if (attachmentsPickerMode == Images) {
            val images = storageHelper.getMedia().map { AttachmentPickerItemState(attachmentMetaData = it) }
            this.attachments = images
        } else if (attachmentsPickerMode == Files) {
            val files = storageHelper.getFiles().map { AttachmentPickerItemState(attachmentMetaData = it) }
            this.attachments = files
        }
    }

    /**
     * Triggered when an [AttachmentMetaData] is selected in the list.
     *
     * @param attachmentItem The selected item.
     */
    public fun changeSelectedAttachments(attachmentItem: AttachmentPickerItemState) {
        val itemIndex = attachments.indexOf(attachmentItem)
        if (itemIndex == -1) return

        val currentItem = attachments[itemIndex]
        val updatedAttachments = if (currentItem.selection is Selection.Selected) {
            val removedCount = currentItem.selection.count
            attachments.mapIndexed { index, item ->
                when {
                    index == itemIndex -> item.copy(selection = Selection.Unselected)
                    item.selection is Selection.Selected && item.selection.count > removedCount ->
                        item.copy(selection = Selection.Selected(count = item.selection.count - 1))
                    else -> item
                }
            }
        } else {
            val nextSelectionCount = attachments.count(AttachmentPickerItemState::isSelected) + 1
            attachments.mapIndexed { index, item ->
                if (index == itemIndex) {
                    item.copy(selection = Selection.Selected(count = nextSelectionCount))
                } else {
                    item
                }
            }
        }

        attachments = updatedAttachments
    }

    /**
     * Loads up the currently selected attachments and maps them to [Attachment] objects
     * based on their type.
     */
    public fun getSelectedAttachments(): List<Attachment> {
        val selectedAttachments = attachments.filter { it.isSelected }

        return storageHelper.getAttachmentsForUpload(selectedAttachments.map { it.attachmentMetaData })
    }

    /**
     * Loads up the currently selected attachments. It uses the [attachmentsPickerMode] to know which
     * attachments to use - files or images.
     *
     * @param onComplete The callback passing the selected attachments.
     */
    internal fun getSelectedAttachmentsAsync(onComplete: (List<Attachment>) -> Unit) {
        viewModelScope.launch {
            val attachments = withContext(DispatcherProvider.IO) {
                getSelectedAttachments()
            }
            onComplete(attachments)
        }
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
     * Transforms the selected meta data into a list of [Attachment]s we can upload.
     *
     * @param metadata List of attachment meta data items.
     * @param onComplete The callback passing the resolved attachments.
     */
    internal fun getAttachmentsFromMetadataAsync(
        metadata: List<AttachmentMetaData>,
        onComplete: (List<Attachment>) -> Unit,
    ) {
        viewModelScope.launch {
            val attachments = withContext(DispatcherProvider.IO) {
                getAttachmentsFromMetaData(metadata)
            }
            onComplete(attachments)
        }
    }

    /**
     * Triggered when we dismiss the attachments picker. Resets the picker mode to images and
     * clears the list until the user needs them again.
     */
    public fun dismissAttachments() {
        attachmentsPickerMode = Images
        attachments = emptyList()
    }
}
