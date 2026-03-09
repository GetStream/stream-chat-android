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
import kotlinx.coroutines.channels.Channel as CoroutineChannel

/**
 * ViewModel for the attachment picker. Drives picker tab state, device storage browsing,
 * and the `isSelected` checkmarks shown in [attachments].
 *
 * Note: [attachments] reflects only the checkmark selection, not the full attachment list
 * staged for the message. The composer attachment list is owned by [MessageComposerViewModel].
 *
 * The active tab and checkmark selection survive process death (e.g. "Don't keep activities").
 * Checkmarks are not reset on hide — they persist until the session is explicitly consumed
 * (e.g. after a message is sent, a poll is created, or a command is selected).
 *
 * @param storageHelper Provides device storage queries and attachment conversion.
 * @param channelState Provides the current [ChannelState] for channel-specific configuration.
 * @param savedStateHandle Persists picker tab and selection state across process death.
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

    // URI strings of items checked by the user. Drives isSelected in attachments.
    // Separate from the composer's attachment list — shared across gallery and file tabs.
    // Persisted so checkmarks survive process death (e.g. camera launch).
    private val _selectedUris = MutableStateFlow(
        savedStateHandle.get<ArrayList<String>>(KeySelectedUris)?.toSet() ?: emptySet(),
    )
    private val _isPickerVisible = MutableStateFlow(
        savedStateHandle[KeyPickerVisible] ?: false,
    )
    private val _submittedAttachments = CoroutineChannel<SubmittedAttachments>(capacity = UNLIMITED)
    private var loadAttachmentsJob: Job? = null

    /**
     * The active picker tab.
     */
    public val pickerMode: AttachmentPickerMode? by _pickerMode.asState(viewModelScope)

    /**
     * Whether the attachment picker is currently visible.
     */
    public val isPickerVisible: Boolean by _isPickerVisible.asState(viewModelScope)

    /**
     * The attachment list for the active [pickerMode], with each item's [AttachmentPickerItemState.isSelected]
     * reflecting the current picker selection.
     */
    public val attachments: List<AttachmentPickerItemState> by combine(
        _pickerMode,
        _mediaItems,
        _fileItems,
        _selectedUris,
    ) { mode, media, files, uris ->
        val items = when (mode) {
            is GalleryPickerMode, null -> media
            is FilePickerMode -> files
            else -> emptyList()
        }
        items.map { meta -> AttachmentPickerItemState(meta, isSelected = meta.uri?.toString() in uris) }
    }.asState(viewModelScope, emptyList())

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
     * Shows or hides the attachment picker. Hiding clears cached media data but preserves the
     * current selection so checkmarks remain when the picker is reopened.
     *
     * @param visible `true` to show the picker, `false` to hide it.
     */
    public fun setPickerVisible(visible: Boolean) {
        _isPickerVisible.value = visible
        savedStateHandle[KeyPickerVisible] = visible
        if (!visible) resetPickerState()
    }

    /**
     * Toggles the attachment picker visibility.
     */
    public fun togglePickerVisibility() {
        setPickerVisible(visible = !_isPickerVisible.value)
    }

    /**
     * Selects [uriString] in the picker, respecting the active picker mode's multi-select setting.
     *
     * In single-select mode, all existing selections are cleared before the new item is selected,
     * and the previously selected URIs are returned so callers can react (e.g. remove from the composer).
     * In multi-select mode, the item is added to the existing selection and an empty set is returned.
     *
     * @param uriString The URI string of the item to select.
     * @return The set of URI strings that were cleared in single-select mode, or an empty set in multi-select mode.
     */
    internal fun selectItem(uriString: String): Set<String> {
        val multiSelect = when (val mode = pickerMode) {
            is GalleryPickerMode -> mode.allowMultipleSelection
            is FilePickerMode -> mode.allowMultipleSelection
            else -> false
        }
        val replaced = if (!multiSelect) {
            _selectedUris.value.also { clearSelection() }
        } else {
            emptySet()
        }
        addToSelection(uriString)
        return replaced
    }

    private fun addToSelection(uriString: String) {
        _selectedUris.value += uriString
        savedStateHandle[KeySelectedUris] = ArrayList(_selectedUris.value)
    }

    /**
     * Removes [uriString] from the picker selection. Has no effect if not selected.
     *
     * @param uriString The URI string of the item to deselect.
     */
    internal fun removeFromSelection(uriString: String) {
        _selectedUris.value -= uriString
        savedStateHandle[KeySelectedUris] = ArrayList(_selectedUris.value)
    }

    /**
     * Clears all picker selections. Call this when the selection is consumed
     * (e.g. after a message is sent, a poll is created, or a command is selected).
     */
    internal fun clearSelection() {
        _selectedUris.value = emptySet()
        savedStateHandle.remove<ArrayList<String>>(KeySelectedUris)
    }

    /**
     * Converts the given [metaData] into lightweight [Attachment]s ready to be staged in the composer.
     *
     * @param metaData The metadata items to convert.
     */
    public fun getAttachmentsFromMetadata(metaData: List<AttachmentMetaData>): List<Attachment> =
        storageHelper.toAttachments(metaData)

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

    private fun resetPickerState() {
        _pickerMode.value = null
        savedStateHandle[KeyPickerMode] = null as String?
        _mediaItems.value = emptyList()
        _fileItems.value = emptyList()
    }
}

private const val KeyPickerVisible = "stream_picker_visible"
private const val KeyPickerMode = "stream_picker_mode"
private const val KeySelectedUris = "stream_selected_uris"

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

// Custom AttachmentPickerMode implementations cannot be serialized; they save as "unknown"
// and restore as null (i.e. the active tab is not preserved across process death for custom modes).
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
