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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.getstream.chat.android.client.channel.state.ChannelState
import io.getstream.chat.android.compose.state.messages.attachments.AttachmentPickerItemState
import io.getstream.chat.android.compose.state.messages.attachments.AttachmentPickerItemState.Selection
import io.getstream.chat.android.compose.state.messages.attachments.AttachmentPickerMode
import io.getstream.chat.android.compose.ui.util.StorageHelperWrapper
import io.getstream.chat.android.compose.util.extensions.asState
import io.getstream.chat.android.core.internal.coroutines.DispatcherProvider
import io.getstream.chat.android.models.Attachment
import io.getstream.chat.android.models.Channel
import io.getstream.chat.android.ui.common.helper.internal.StorageHelper
import io.getstream.chat.android.ui.common.state.messages.composer.AttachmentMetaData
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * ViewModel responsible for handling the state and business logic of the attachment picker.
 *
 * This ViewModel loads media or files based on the current picker mode, keeps track of the selection state,
 * and prepares the selected items for upload. It is used by attachment picker UIs to show available files/media
 * and manage user selections.
 *
 * @param storageHelper A wrapper for the [StorageHelper]
 * that helps with accessing and processing files from the device storage.
 * @param channelState A [StateFlow] that provides the current [ChannelState], used to access channel-specific
 * information and configuration.
 */
public class AttachmentsPickerViewModel(
    private val storageHelper: StorageHelperWrapper,
    channelState: StateFlow<ChannelState?>,
) : ViewModel() {

    /**
     * The information for the current [Channel].
     */
    @OptIn(ExperimentalCoroutinesApi::class)
    public val channel: Channel by channelState
        .filterNotNull()
        .flatMapLatest { state ->
            combine(
                state.channelData,
                state.channelConfig,
            ) { _, _ ->
                state.toChannel()
            }
        }
        .asState(viewModelScope, Channel())

    /**
     * Currently selected attachment picker mode.
     */
    public var pickerMode: AttachmentPickerMode? by mutableStateOf(null)
        private set

    /**
     * List of attachments available for the current picker mode.
     */
    public var attachments: List<AttachmentPickerItemState> by mutableStateOf(emptyList())

    /**
     * Gives us information if we're showing the attachments picker or not.
     */
    public var isShowingAttachments: Boolean by mutableStateOf(false)
        private set

    /**
     * Changes the currently selected [AttachmentPickerMode] and loads the required data.
     * If no permission is granted, it will not try to load data to avoid crashes.
     *
     * @param pickerMode The currently selected picker mode.
     */
    public fun changePickerMode(pickerMode: AttachmentPickerMode) {
        this.pickerMode = pickerMode
    }

    /**
     * Notifies the ViewModel if we should show attachments or not.
     *
     * @param showAttachments If we need to show attachments or hide them.
     */
    public fun changeAttachmentState(showAttachments: Boolean) {
        isShowingAttachments = showAttachments

        if (!showAttachments) {
            resetState()
        }
    }

    /**
     * Toggles the visibility of the attachments picker.
     */
    public fun toggleAttachmentState() {
        changeAttachmentState(showAttachments = !isShowingAttachments)
    }

    /**
     * Removes the selection for an attachment that matches the given [Attachment].
     * This is used to sync the picker state when an attachment is removed from the composer.
     *
     * @param attachment The attachment to unselect.
     */
    public fun removeSelectedAttachment(attachment: Attachment) {
        val itemIndex = attachments.indexOfFirst { item ->
            // Safe: attachment.name is set from attachmentMetaData.title when the attachment is created
            item.attachmentMetaData.title == attachment.name
        }
        if (itemIndex == -1) return

        val currentItem = attachments[itemIndex]
        if (currentItem.selection !is Selection.Selected) return

        val removedPosition = currentItem.selection.position
        attachments = attachments.mapIndexed { index, item ->
            when {
                index == itemIndex -> item.copy(selection = Selection.Unselected)
                item.selection is Selection.Selected && item.selection.position > removedPosition ->
                    item.copy(selection = Selection.Selected(position = item.selection.position - 1))

                else -> item
            }
        }
    }

    /**
     * Triggered when an [AttachmentMetaData] is selected in the list.
     *
     * @param attachmentItem The selected item.
     * @param allowMultipleSelection When `true`, multiple items can be selected. When `false`,
     * only one item can be selected at a time. Defaults to `true`.
     */
    public fun changeSelectedAttachments(
        attachmentItem: AttachmentPickerItemState,
        allowMultipleSelection: Boolean = true,
    ) {
        val itemIndex = attachments.indexOf(attachmentItem)
        if (itemIndex == -1) return

        val isCurrentlySelected = attachments[itemIndex].isSelected

        // Single-select: clicking already selected item is a no-op
        if (!allowMultipleSelection && isCurrentlySelected) return

        attachments = if (isCurrentlySelected) {
            deselectAttachment(itemIndex)
        } else {
            selectAttachment(itemIndex, allowMultipleSelection)
        }
    }

    private fun deselectAttachment(itemIndex: Int): List<AttachmentPickerItemState> {
        val removedPosition = (attachments[itemIndex].selection as Selection.Selected).position
        return attachments.mapIndexed { index, item ->
            when {
                index == itemIndex -> item.copy(selection = Selection.Unselected)
                item.selection is Selection.Selected && item.selection.position > removedPosition ->
                    item.copy(selection = Selection.Selected(position = item.selection.position - 1))
                else -> item
            }
        }
    }

    private fun selectAttachment(itemIndex: Int, allowMultipleSelection: Boolean): List<AttachmentPickerItemState> {
        return if (allowMultipleSelection) {
            val nextPosition = attachments.count(AttachmentPickerItemState::isSelected) + 1
            attachments.mapIndexed { index, item ->
                if (index == itemIndex) item.copy(selection = Selection.Selected(position = nextPosition)) else item
            }
        } else {
            attachments.mapIndexed { index, item ->
                if (index == itemIndex) {
                    item.copy(selection = Selection.Selected(position = 1))
                } else {
                    item.copy(selection = Selection.Unselected)
                }
            }
        }
    }

    /**
     * Loads up the currently selected attachments and maps them to [Attachment] objects.
     */
    public fun getSelectedAttachments(): List<Attachment> =
        attachments.filter(AttachmentPickerItemState::isSelected)
            .map(AttachmentPickerItemState::attachmentMetaData)
            .let(storageHelper::getAttachmentsForUpload)

    /**
     * Asynchronously loads up the currently selected attachments and maps them to [Attachment] objects.
     *
     * @param onComplete The callback called when the attachments are loaded.
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

    private fun resetState() {
        pickerMode = null
        attachments = emptyList()
    }
}
