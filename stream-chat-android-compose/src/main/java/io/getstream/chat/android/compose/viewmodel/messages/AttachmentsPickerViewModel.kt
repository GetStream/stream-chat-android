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
import io.getstream.chat.android.compose.state.messages.attachments.AttachmentPickerMode
import io.getstream.chat.android.compose.state.messages.attachments.FilePickerMode
import io.getstream.chat.android.compose.state.messages.attachments.GalleryPickerMode
import io.getstream.chat.android.compose.util.extensions.asState
import io.getstream.chat.android.core.internal.coroutines.DispatcherProvider
import io.getstream.chat.android.models.Attachment
import io.getstream.chat.android.models.Channel
import io.getstream.chat.android.ui.common.helper.internal.AttachmentStorageHelper
import io.getstream.chat.android.ui.common.helper.internal.AttachmentStorageHelper.Companion.EXTRA_SOURCE_URI
import io.getstream.chat.android.ui.common.state.messages.composer.AttachmentMetaData
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * ViewModel for the attachment picker. Manages available media and file attachments,
 * user selection state, and conversion to uploadable [Attachment] objects.
 *
 * Media and file items are stored in separate lists so selections survive tab switches.
 * Items that appear in both tabs (e.g. an image visible in the gallery and the files list)
 * share selection state — selecting in one tab marks the matching item in the other.
 *
 * @param storageHelper Provides device storage queries and attachment conversion.
 * @param channelState Provides the current [ChannelState] for channel-specific configuration.
 */
public class AttachmentsPickerViewModel(
    private val storageHelper: AttachmentStorageHelper,
    channelState: StateFlow<ChannelState?>,
) : ViewModel() {

    /**
     * The current [Channel] information.
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
     * The active picker tab.
     */
    public var pickerMode: AttachmentPickerMode? by mutableStateOf(null)
        private set

    /**
     * Items from the device gallery (images and videos).
     */
    private var mediaAttachments: List<AttachmentPickerItemState> by mutableStateOf(emptyList())

    /**
     * Items from the device file system.
     */
    private var fileAttachments: List<AttachmentPickerItemState> by mutableStateOf(emptyList())

    /**
     * The attachment list for the active [pickerMode].
     *
     * Reading returns [mediaAttachments] or [fileAttachments] depending on the mode.
     * Writing updates the corresponding list. Defaults to [mediaAttachments] when the mode is `null`.
     */
    public var attachments: List<AttachmentPickerItemState>
        get() = when (pickerMode) {
            is GalleryPickerMode, null -> mediaAttachments
            is FilePickerMode -> fileAttachments
            else -> emptyList()
        }
        set(value) {
            when (pickerMode) {
                is GalleryPickerMode, null -> mediaAttachments = value
                is FilePickerMode -> fileAttachments = value
                else -> { /* Other modes have no selectable attachments */ }
            }
        }

    /**
     * Whether the attachment picker is currently visible.
     */
    public var isShowingAttachments: Boolean by mutableStateOf(false)
        private set

    /**
     * Switches the active picker tab.
     */
    public fun changePickerMode(pickerMode: AttachmentPickerMode) {
        this.pickerMode = pickerMode
    }

    /**
     * Replaces the current tab's items with [newItems], preserving any existing selections.
     *
     * Existing selections are restored by matching on [AttachmentPickerItemState.attachmentMetaData].
     * Items that match a selected item in the other tab are also marked as selected.
     */
    public fun onAttachmentsLoaded(newItems: List<AttachmentPickerItemState>) {
        val merged = mergeSelections(existing = attachments, newItems = newItems)
        attachments = applyCrossTabSelections(merged, otherAttachments)
    }

    /**
     * Shows or hides the attachment picker. Hiding clears all state.
     */
    public fun changeAttachmentState(showAttachments: Boolean) {
        isShowingAttachments = showAttachments

        if (!showAttachments) {
            resetState()
        }
    }

    /**
     * Toggles the attachment picker visibility.
     */
    public fun toggleAttachmentState() {
        changeAttachmentState(showAttachments = !isShowingAttachments)
    }

    /**
     * Deselects the picker item whose content URI matches the [EXTRA_SOURCE_URI] stored
     * in [attachment]'s [extraData][Attachment.extraData]. Both tabs are checked.
     */
    public fun removeSelectedAttachment(attachment: Attachment) {
        val sourceUri = (attachment.extraData[EXTRA_SOURCE_URI] as? String)
            ?.let(Uri::parse) ?: return
        deselectByUri(sourceUri, ::mediaAttachments) { mediaAttachments = it }
        deselectByUri(sourceUri, ::fileAttachments) { fileAttachments = it }
    }

    /**
     * Selects or deselects [attachmentItem] in the current tab.
     *
     * @param allowMultipleSelection When `false`, selecting a new item deselects all others
     * in the current tab.
     */
    public fun changeSelectedAttachments(
        attachmentItem: AttachmentPickerItemState,
        allowMultipleSelection: Boolean = true,
    ) {
        val itemIndex = attachments.indexOf(attachmentItem)
        if (itemIndex == -1) return

        val isCurrentlySelected = attachments[itemIndex].isSelected

        if (!allowMultipleSelection && isCurrentlySelected) return

        val previous = attachments
        attachments = if (isCurrentlySelected) {
            deselectAttachment(itemIndex)
        } else {
            selectAttachment(itemIndex, allowMultipleSelection)
        }
        syncChangesToOtherTab(previous, attachments)
    }

    /**
     * Returns lightweight preview [Attachment] objects for all selected items across both tabs.
     *
     * No file copying is performed; the returned attachments carry only the source URI and
     * metadata needed for the composer preview. File resolution is deferred to send time
     * via [AttachmentStorageHelper.resolveAttachmentFiles].
     */
    public fun getSelectedAttachments(): List<Attachment> {
        val allSelected = (mediaAttachments + fileAttachments)
            .filter(AttachmentPickerItemState::isSelected)
            .distinctBy { it.attachmentMetaData.uri }
            .map(AttachmentPickerItemState::attachmentMetaData)
        return storageHelper.toAttachments(allSelected)
    }

    /**
     * Converts the given [metaData] into lightweight [Attachment]s.
     *
     * File resolution is deferred to send time via [AttachmentStorageHelper.resolveAttachmentFiles].
     */
    public fun getAttachmentsFromMetaData(metaData: List<AttachmentMetaData>): List<Attachment> =
        storageHelper.toAttachments(metaData)

    /**
     * One-shot events for attachments resolved from system picker URIs.
     * Collected by the parent composable to submit attachments and show error toasts.
     */
    internal val submittedAttachments: SharedFlow<SubmittedAttachments>
        get() = _submittedAttachments.asSharedFlow()

    private val _submittedAttachments = MutableSharedFlow<SubmittedAttachments>(extraBufferCapacity = 1)

    /**
     * Loads attachment metadata from device storage for the current [pickerMode].
     *
     * Results are written directly to [attachments] via [onAttachmentsLoaded];
     * callers do not need a callback.
     */
    internal fun loadAttachments() {
        viewModelScope.launch {
            val metadata = withContext(DispatcherProvider.IO) {
                when (pickerMode) {
                    is GalleryPickerMode -> storageHelper.getMediaMetadata()
                    is FilePickerMode -> storageHelper.getFileMetadata()
                    else -> emptyList()
                }
            }
            onAttachmentsLoaded(metadata.map(::AttachmentPickerItemState))
        }
    }

    /**
     * Resolves [uris] from a system picker into [Attachment]s and emits the result
     * via [submittedAttachments].
     */
    internal fun resolveAndSubmitUris(uris: List<Uri>) {
        if (uris.isEmpty()) return
        viewModelScope.launch {
            val metadata = withContext(DispatcherProvider.IO) { storageHelper.resolveMetadata(uris) }
            val attachments = storageHelper.toAttachments(metadata)
            _submittedAttachments.tryEmit(
                SubmittedAttachments(
                    attachments = attachments,
                    hasUnsupportedFiles = metadata.size < uris.size,
                ),
            )
        }
    }

    /**
     * The inactive tab's attachment list (opposite of [attachments]).
     */
    private var otherAttachments: List<AttachmentPickerItemState>
        get() = when (pickerMode) {
            is GalleryPickerMode, null -> fileAttachments
            is FilePickerMode -> mediaAttachments
            else -> emptyList()
        }
        set(value) {
            when (pickerMode) {
                is GalleryPickerMode, null -> fileAttachments = value
                is FilePickerMode -> mediaAttachments = value
                else -> { /* Other modes have no selectable attachments */ }
            }
        }

    private fun resetState() {
        pickerMode = null
        mediaAttachments = emptyList()
        fileAttachments = emptyList()
    }

    private fun deselectAttachment(itemIndex: Int): List<AttachmentPickerItemState> =
        attachments.mapIndexed { index, item ->
            if (index == itemIndex) item.copy(isSelected = false) else item
        }

    private fun selectAttachment(itemIndex: Int, allowMultipleSelection: Boolean): List<AttachmentPickerItemState> =
        attachments.mapIndexed { index, item ->
            when {
                index == itemIndex -> item.copy(isSelected = true)
                !allowMultipleSelection -> item.copy(isSelected = false)
                else -> item
            }
        }

    /**
     * Propagates selection changes from the current tab to the other tab.
     * Items are matched across tabs by [URI][AttachmentMetaData.uri].
     */
    private fun syncChangesToOtherTab(
        previous: List<AttachmentPickerItemState>,
        current: List<AttachmentPickerItemState>,
    ) {
        if (otherAttachments.isEmpty()) return

        val changes = current.zip(previous)
            .filter { (cur, prev) -> cur.isSelected != prev.isSelected }
            .mapNotNull { (cur, _) -> cur.attachmentMetaData.uri?.let { it to cur.isSelected } }

        for ((uri, selected) in changes) {
            if (selected) {
                selectByUri(uri, { otherAttachments }) { otherAttachments = it }
            } else {
                deselectByUri(uri, { otherAttachments }) { otherAttachments = it }
            }
        }
    }

    internal companion object {

        /**
         * Restores selections from [existing] into [newItems] by matching on
         * [AttachmentPickerItemState.attachmentMetaData] equality.
         */
        internal fun mergeSelections(
            existing: List<AttachmentPickerItemState>,
            newItems: List<AttachmentPickerItemState>,
        ): List<AttachmentPickerItemState> {
            if (existing.isEmpty()) return newItems
            val selectedMetaData = existing
                .filter(AttachmentPickerItemState::isSelected)
                .map(AttachmentPickerItemState::attachmentMetaData)
                .toSet()
            if (selectedMetaData.isEmpty()) return newItems
            return newItems.map { item ->
                if (item.attachmentMetaData in selectedMetaData) item.copy(isSelected = true) else item
            }
        }

        /**
         * Marks items in [items] as selected when a matching item (by [URI][AttachmentMetaData.uri])
         * is already selected in [otherTabItems].
         */
        internal fun applyCrossTabSelections(
            items: List<AttachmentPickerItemState>,
            otherTabItems: List<AttachmentPickerItemState>,
        ): List<AttachmentPickerItemState> {
            val otherSelectedUris = otherTabItems
                .filter(AttachmentPickerItemState::isSelected)
                .mapNotNull { it.attachmentMetaData.uri }
                .toSet()
            if (otherSelectedUris.isEmpty()) return items

            return items.map { item ->
                val uri = item.attachmentMetaData.uri
                if (!item.isSelected && uri != null && uri in otherSelectedUris) {
                    item.copy(isSelected = true)
                } else {
                    item
                }
            }
        }

        /**
         * Finds an unselected item whose [AttachmentMetaData.uri] equals [uri] and selects it.
         * Calls [update] with the result if a match is found.
         */
        private fun selectByUri(
            uri: Uri,
            items: () -> List<AttachmentPickerItemState>,
            update: (List<AttachmentPickerItemState>) -> Unit,
        ) {
            val list = items()
            val matchIndex = list.indexOfFirst { it.attachmentMetaData.uri == uri }
            if (matchIndex == -1 || list[matchIndex].isSelected) return

            update(
                list.mapIndexed { index, item ->
                    if (index == matchIndex) item.copy(isSelected = true) else item
                },
            )
        }

        /**
         * Finds a selected item whose [AttachmentMetaData.uri] equals [uri] and deselects it.
         * Calls [update] with the result if a match is found.
         */
        private fun deselectByUri(
            uri: Uri,
            items: () -> List<AttachmentPickerItemState>,
            update: (List<AttachmentPickerItemState>) -> Unit,
        ) {
            val list = items()
            val matchIndex = list.indexOfFirst { it.attachmentMetaData.uri == uri }
            if (matchIndex == -1 || !list[matchIndex].isSelected) return

            update(
                list.mapIndexed { index, item ->
                    if (index == matchIndex) item.copy(isSelected = false) else item
                },
            )
        }
    }
}

/**
 * Event emitted when system picker URIs have been resolved into [Attachment]s.
 *
 * @property attachments The resolved attachments ready for the composer.
 * @property hasUnsupportedFiles `true` when some URIs were filtered out as unsupported.
 */
internal data class SubmittedAttachments(
    val attachments: List<Attachment>,
    val hasUnsupportedFiles: Boolean,
)
