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
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.getstream.chat.android.client.channel.state.ChannelState
import io.getstream.chat.android.compose.state.messages.attachments.AttachmentPickerItemState
import io.getstream.chat.android.compose.state.messages.attachments.AttachmentPickerMode
import io.getstream.chat.android.compose.state.messages.attachments.CameraPickerMode
import io.getstream.chat.android.compose.state.messages.attachments.CommandPickerMode
import io.getstream.chat.android.compose.state.messages.attachments.FilePickerMode
import io.getstream.chat.android.compose.state.messages.attachments.GalleryPickerMode
import io.getstream.chat.android.compose.state.messages.attachments.PollPickerMode
import io.getstream.chat.android.compose.util.extensions.asState
import io.getstream.chat.android.core.internal.coroutines.DispatcherProvider
import io.getstream.chat.android.models.Attachment
import io.getstream.chat.android.models.Channel
import io.getstream.chat.android.ui.common.helper.internal.AttachmentStorageHelper
import io.getstream.chat.android.ui.common.helper.internal.AttachmentStorageHelper.Companion.EXTRA_SOURCE_URI
import io.getstream.chat.android.ui.common.state.messages.composer.AttachmentMetaData
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel.Factory.UNLIMITED
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * ViewModel for the attachment picker. Manages available media and file attachments,
 * user selection state, and conversion to uploadable [Attachment] objects.
 *
 * Media and file items are stored in separate lists so they survive tab switches.
 * Selection is centralised in a single [Set] of [Uri]s, so items that appear in both
 * tabs (e.g. an image visible in the gallery and the files list) share selection state
 * automatically — no explicit cross-tab synchronisation is needed.
 *
 * Picker visibility and active tab survive Activity destruction (e.g. "Don't keep
 * activities") so that pending system picker results are delivered on recreation.
 *
 * @param storageHelper Provides device storage queries and attachment conversion.
 * @param channelState Provides the current [ChannelState] for channel-specific configuration.
 * @param savedStateHandle Persists picker visibility and mode across Activity recreation.
 */
public class AttachmentsPickerViewModel(
    private val storageHelper: AttachmentStorageHelper,
    channelState: StateFlow<ChannelState?>,
    private val savedStateHandle: SavedStateHandle = SavedStateHandle(),
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

    private val _pickerMode = MutableStateFlow(
        savedStateHandle.get<String>(KeyPickerMode)?.toPickerMode(),
    )
    private val _mediaItems = MutableStateFlow<List<AttachmentMetaData>>(emptyList())
    private val _fileItems = MutableStateFlow<List<AttachmentMetaData>>(emptyList())
    private val _selectedUris = MutableStateFlow<Set<Uri>>(linkedSetOf())
    private val _isPickerVisible = MutableStateFlow(
        savedStateHandle[KeyPickerVisible] ?: false,
    )

    /**
     * The active picker tab.
     */
    public val pickerMode: AttachmentPickerMode? by _pickerMode.asState(viewModelScope)

    /**
     * Whether the attachment picker is currently visible.
     */
    public val isPickerVisible: Boolean by _isPickerVisible.asState(viewModelScope)

    /**
     * The attachment list for the active [pickerMode], with each item's selection state
     * reflecting whether it is currently selected.
     */
    public val attachments: List<AttachmentPickerItemState> by combine(
        _pickerMode,
        _mediaItems,
        _fileItems,
        _selectedUris,
    ) { mode, media, files, selected ->
        val items = when (mode) {
            is GalleryPickerMode, null -> media
            is FilePickerMode -> files
            else -> emptyList()
        }
        items.map { meta -> AttachmentPickerItemState(meta, isSelected = meta.uri in selected) }
    }.asState(viewModelScope, emptyList())

    private val _submittedAttachments = kotlinx.coroutines.channels.Channel<SubmittedAttachments>(capacity = UNLIMITED)

    /**
     * One-shot events for attachments resolved from system picker URIs.
     * Collected by the parent composable to submit attachments and show error toasts.
     *
     * Each event is retained until consumed, so it is safe to collect even if the
     * collector starts after the emission.
     */
    public val submittedAttachments: Flow<SubmittedAttachments>
        get() = _submittedAttachments.receiveAsFlow()

    /**
     * Switches the active picker tab.
     *
     * @param mode The [AttachmentPickerMode] to activate.
     */
    public fun setPickerMode(mode: AttachmentPickerMode) {
        _pickerMode.value = mode
        savedStateHandle[KeyPickerMode] = mode.toSavedKey()
    }

    /**
     * Shows or hides the attachment picker. Hiding clears cached data but preserves the
     * current selection so items remain checked when the picker is reopened.
     *
     * Call [clearSelection] after this to also reset the selection (e.g. after sending a message).
     *
     * @param visible `true` to show the picker, `false` to hide it.
     */
    public fun setPickerVisible(visible: Boolean) {
        _isPickerVisible.value = visible
        savedStateHandle[KeyPickerVisible] = visible
        if (!visible) clearCachedData()
    }

    /**
     * Toggles the attachment picker visibility.
     */
    public fun togglePickerVisibility() {
        setPickerVisible(visible = !_isPickerVisible.value)
    }

    /**
     * Selects or deselects [item].
     *
     * @param item The attachment item to select or deselect.
     * @param allowMultipleSelection When `false`, selecting a new item replaces the
     * current selection. Tapping the already-selected item is a no-op.
     */
    public fun toggleSelection(
        item: AttachmentPickerItemState,
        allowMultipleSelection: Boolean = true,
    ) {
        val uri = item.attachmentMetaData.uri ?: return
        val currentlySelected = uri in _selectedUris.value

        if (!allowMultipleSelection && currentlySelected) return

        _selectedUris.value = if (currentlySelected) {
            _selectedUris.value - uri
        } else {
            if (allowMultipleSelection) _selectedUris.value + uri else linkedSetOf(uri)
        }
    }

    /**
     * Deselects the attachment whose content URI matches the [EXTRA_SOURCE_URI] stored
     * in [attachment]'s [extraData][Attachment.extraData].
     *
     * @param attachment The [Attachment] to deselect.
     */
    public fun deselectAttachment(attachment: Attachment) {
        val sourceUri = (attachment.extraData[EXTRA_SOURCE_URI] as? String)
            ?.let(Uri::parse) ?: return
        _selectedUris.value -= sourceUri
    }

    /**
     * Returns lightweight preview [Attachment] objects for all selected items across both tabs,
     * ordered by the sequence in which the user selected them.
     *
     * Items that appear in both tabs are deduplicated by URI.
     * No file copying is performed; file resolution is deferred to send time
     * via [AttachmentStorageHelper.resolveAttachmentFiles].
     */
    public fun getSelectedAttachments(): List<Attachment> {
        val allItemsByUri = (_mediaItems.value + _fileItems.value)
            .mapNotNull { meta -> meta.uri?.let { it to meta } }
            .toMap()
        val orderedMeta = _selectedUris.value.mapNotNull(allItemsByUri::get)
        return storageHelper.toAttachments(orderedMeta)
    }

    /**
     * Converts the given [metaData] into lightweight [Attachment]s.
     *
     * File resolution is deferred to send time via [AttachmentStorageHelper.resolveAttachmentFiles].
     *
     * @param metaData The metadata items to convert.
     */
    public fun getAttachmentsFromMetadata(metaData: List<AttachmentMetaData>): List<Attachment> =
        storageHelper.toAttachments(metaData)

    private var loadAttachmentsJob: Job? = null

    /**
     * Loads attachment metadata from device storage for the current [pickerMode].
     */
    public fun loadAttachments() {
        loadAttachmentsJob?.cancel()
        loadAttachmentsJob = viewModelScope.launch {
            val metadata = withContext(DispatcherProvider.IO) {
                when (_pickerMode.value) {
                    is GalleryPickerMode, null -> storageHelper.getMediaMetadata()
                    is FilePickerMode -> storageHelper.getFileMetadata()
                    else -> emptyList()
                }
            }
            setCurrentTabItems(metadata)
        }
    }

    /**
     * Resolves [uris] from a system picker into [Attachment]s and emits the result
     * via [submittedAttachments].
     *
     * @param uris Content URIs returned by the system picker.
     */
    public fun resolveAndSubmitUris(uris: List<Uri>) {
        if (uris.isEmpty()) return
        viewModelScope.launch {
            val metadata = withContext(DispatcherProvider.IO) { storageHelper.resolveMetadata(uris) }
            val attachments = storageHelper.toAttachments(metadata)
            _submittedAttachments.trySend(
                SubmittedAttachments(
                    attachments = attachments,
                    hasUnsupportedFiles = metadata.size < uris.size,
                ),
            )
        }
    }

    private fun setCurrentTabItems(items: List<AttachmentMetaData>) {
        when (_pickerMode.value) {
            is GalleryPickerMode, null -> _mediaItems.value = items
            is FilePickerMode -> _fileItems.value = items
            else -> Unit
        }
    }

    /**
     * Removes all selected URIs. Call this when the associated attachments are consumed
     * (e.g. message sent, poll created) so the picker starts fresh on next open.
     */
    public fun clearSelection() {
        _selectedUris.value = linkedSetOf()
    }

    private fun clearCachedData() {
        _pickerMode.value = null
        savedStateHandle[KeyPickerMode] = null as String?
        _mediaItems.value = emptyList()
        _fileItems.value = emptyList()
    }
}

private const val KeyPickerVisible = "stream_picker_visible"
private const val KeyPickerMode = "stream_picker_mode"

/**
 * Event emitted when system picker URIs have been resolved into [Attachment]s.
 *
 * @property attachments The resolved attachments ready for the composer.
 * @property hasUnsupportedFiles `true` when some URIs were filtered out as unsupported.
 */
public data class SubmittedAttachments(
    val attachments: List<Attachment>,
    val hasUnsupportedFiles: Boolean,
)

private fun AttachmentPickerMode.toSavedKey(): String = when (this) {
    is GalleryPickerMode -> "gallery"
    is FilePickerMode -> "file"
    is CameraPickerMode -> "camera"
    is PollPickerMode -> "poll"
    is CommandPickerMode -> "command"
    else -> "unknown"
}

private fun String.toPickerMode(): AttachmentPickerMode? = when (this) {
    "gallery" -> GalleryPickerMode()
    "file" -> FilePickerMode()
    "camera" -> CameraPickerMode()
    "poll" -> PollPickerMode()
    "command" -> CommandPickerMode
    else -> null
}
