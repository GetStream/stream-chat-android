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

/**
 * ViewModel for the attachment picker. Manages storage browsing and picker UI state.
 *
 * **Responsibilities:**
 * - Active picker tab ([pickerMode]) and visibility ([isPickerVisible])
 * - Loading media and file metadata from device storage ([loadAttachments])
 * - Resolving system-picker URIs into [Attachment]s ([resolveAndSubmitUris])
 * - Tracking which grid items the user has selected via [gridSelectedUris], used to drive
 *   `isSelected` checkmarks in the attachment grid
 *
 * **Not responsible for:** the full content or ordering of the message's attachment list.
 * That is owned by [MessageComposerViewModel], which is the single source of truth for all
 * attachments in the current composer session.
 *
 * The [gridSelectedUris] index and picker tab survive Activity destruction
 * (e.g. "Don't keep activities") via [savedStateHandle].
 * [gridSelectedUris] is cleared by [clearGridSelection] when the selection is consumed
 * (e.g. message sent, poll created, command selected).
 *
 * @param storageHelper Provides device storage queries and attachment conversion.
 * @param channelState Provides the current [ChannelState] for channel-specific configuration.
 * @param savedStateHandle Persists picker state across Activity recreation.
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

    /**
     * URI strings of grid items (gallery or files tab) currently checked by the user.
     * Used only for driving `isSelected` state in the attachment grid — the full attachment
     * list for the composer is owned by [MessageComposerViewModel].
     *
     * Persisted so checkmarks survive Activity recreation (e.g. when the camera is launched).
     */
    private val _gridSelectedUris = MutableStateFlow<Set<String>>(
        savedStateHandle.get<ArrayList<String>>(KeyGridSelectedUris)?.toSet() ?: emptySet(),
    )
    private val _isPickerVisible = MutableStateFlow(
        savedStateHandle[KeyPickerVisible] ?: false,
    )
    private val _submittedAttachments = kotlinx.coroutines.channels.Channel<SubmittedAttachments>(capacity = UNLIMITED)

    /**
     * The active picker tab.
     */
    public val pickerMode: AttachmentPickerMode? by _pickerMode.asState(viewModelScope)

    /**
     * Whether the attachment picker is currently visible.
     */
    public val isPickerVisible: Boolean by _isPickerVisible.asState(viewModelScope)

    /**
     * URI strings of grid items currently selected by the user, used to show checkmarks.
     */
    internal val gridSelectedUris: StateFlow<Set<String>> = _gridSelectedUris

    /**
     * The attachment list for the active [pickerMode], with each item's [AttachmentPickerItemState.isSelected]
     * reflecting whether it appears in [gridSelectedUris].
     */
    public val attachments: List<AttachmentPickerItemState> by combine(
        _pickerMode,
        _mediaItems,
        _fileItems,
        _gridSelectedUris,
    ) { mode, media, files, gridUris ->
        val items = when (mode) {
            is GalleryPickerMode, null -> media
            is FilePickerMode -> files
            else -> emptyList()
        }
        items.map { meta -> AttachmentPickerItemState(meta, isSelected = meta.uri?.toString() in gridUris) }
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
     * Shows or hides the attachment picker. Hiding clears cached data but preserves the
     * current grid selection so checkmarks remain when the picker is reopened.
     *
     * Call [clearGridSelection] after this to also reset the selection (e.g. after sending a message).
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
     * Marks [uriString] as selected in the grid. Has no effect if already selected.
     *
     * @param uriString The URI string of the grid item to select.
     */
    internal fun addToGridSelection(uriString: String) {
        _gridSelectedUris.value = _gridSelectedUris.value + uriString
        savedStateHandle[KeyGridSelectedUris] = ArrayList(_gridSelectedUris.value)
    }

    /**
     * Removes [uriString] from the grid selection. Has no effect if not selected.
     *
     * @param uriString The URI string of the grid item to deselect.
     */
    internal fun removeFromGridSelection(uriString: String) {
        _gridSelectedUris.value = _gridSelectedUris.value - uriString
        savedStateHandle[KeyGridSelectedUris] = ArrayList(_gridSelectedUris.value)
    }

    /**
     * Clears all grid selections. Call this when the selection is consumed
     * (e.g. after a message is sent, a poll is created, or a command is selected).
     */
    internal fun clearGridSelection() {
        _gridSelectedUris.value = emptySet()
        savedStateHandle.remove<ArrayList<String>>(KeyGridSelectedUris)
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

    private fun clearCachedData() {
        _pickerMode.value = null
        savedStateHandle[KeyPickerMode] = null as String?
        _mediaItems.value = emptyList()
        _fileItems.value = emptyList()
    }
}

private const val KeyPickerVisible = "stream_picker_visible"
private const val KeyPickerMode = "stream_picker_mode"
private const val KeyGridSelectedUris = "stream_picker_grid_selected_uris"

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
