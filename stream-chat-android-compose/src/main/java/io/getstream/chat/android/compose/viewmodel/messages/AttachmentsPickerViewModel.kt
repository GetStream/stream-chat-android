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
import android.os.Build
import android.os.Bundle
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
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * ViewModel for the attachment picker. Manages available media and file attachments,
 * user selection state, and conversion to uploadable [Attachment] objects.
 *
 * This ViewModel is the single source of truth for all attachments in the current composer
 * session. The composer's attachment list is always derived from [getSelectedAttachments],
 * which merges externally-added attachments (camera, system picker) with grid selections.
 *
 * Media and file items are stored in separate lists so they survive tab switches.
 * Selection is centralised in a single [Set] of [Uri]s, so items that appear in both
 * tabs (e.g. an image visible in the gallery and the files list) share selection state
 * automatically — no explicit cross-tab synchronisation is needed.
 *
 * The following state survives Activity destruction (e.g. "Don't keep activities"):
 * - Picker visibility and active tab — so pending system picker results are delivered on recreation.
 * - URI-based grid selection — so gallery/file picks are not lost when the camera is launched.
 * - Externally-added attachments (camera, system picker) — so captures are not lost across
 *   multiple camera sessions.
 *
 * All persisted state is cleared by [clearSelection], which must be called when the associated
 * attachments are consumed (e.g. message sent, poll created, command selected).
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
    private val _selectedUris = MutableStateFlow<Set<Uri>>(
        savedStateHandle.get<ArrayList<Uri>>(KeySelectedUris)?.let(::LinkedHashSet) ?: linkedSetOf(),
    )
    private val _selectedGridAttachments = MutableStateFlow<Map<Uri, Attachment>>(
        savedStateHandle.get<Bundle>(KeySelectedGridAttachments)
            ?.getBundleList(KeySelectedGridAttachmentItems)
            ?.mapNotNull(Bundle::toUriAttachmentPair)
            ?.toMap()
            ?: emptyMap(),
    )
    private val _isPickerVisible = MutableStateFlow(
        savedStateHandle[KeyPickerVisible] ?: false,
    )
    private val _externalAttachments = MutableStateFlow<List<Attachment>>(
        savedStateHandle.get<Bundle>(KeyExternalAttachments)
            ?.getBundleList(KeyExternalAttachmentItems)
            ?.mapNotNull(Bundle::toAttachment)
            ?: emptyList(),
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

        if (currentlySelected) {
            _selectedUris.value = _selectedUris.value - uri
            _selectedGridAttachments.update { it - uri }
        } else {
            val attachment = storageHelper.toAttachments(listOf(item.attachmentMetaData)).firstOrNull()
            if (allowMultipleSelection) {
                _selectedUris.value = _selectedUris.value + uri
                if (attachment != null) _selectedGridAttachments.update { it + (uri to attachment) }
            } else {
                _selectedUris.value = linkedSetOf(uri)
                _selectedGridAttachments.value = if (attachment != null) mapOf(uri to attachment) else emptyMap()
            }
        }
        savedStateHandle[KeySelectedUris] = ArrayList(_selectedUris.value)
        persistSelectedGridAttachments(_selectedGridAttachments.value)
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
        _selectedGridAttachments.update { it - sourceUri }
        savedStateHandle[KeySelectedUris] = ArrayList(_selectedUris.value)
        persistSelectedGridAttachments(_selectedGridAttachments.value)
    }

    /**
     * Returns lightweight preview [Attachment] objects for all selected items across both tabs,
     * ordered by the sequence in which the user selected them, preceded by any externally-added
     * attachments (e.g. from the camera or system file picker).
     *
     * Items that appear in both tabs are deduplicated by URI.
     * No file copying is performed; file resolution is deferred to send time
     * via [AttachmentStorageHelper.resolveAttachmentFiles].
     */
    public fun getSelectedAttachments(): List<Attachment> {
        val orderedGridAttachments = _selectedUris.value.mapNotNull { _selectedGridAttachments.value[it] }
        return _externalAttachments.value + orderedGridAttachments
    }

    /**
     * Adds attachments from one-shot sources (e.g. camera capture, system file picker) to the
     * picker's selection state so that they are included in [getSelectedAttachments] and survive
     * picker close/reopen within the same composer session.
     *
     * These attachments are cleared by [clearSelection] (e.g. after a message is sent).
     *
     * @param attachments The attachments to add.
     */
    public fun addExternalAttachments(attachments: List<Attachment>) {
        _externalAttachments.update { it + attachments }
        persistExternalAttachments(_externalAttachments.value)
    }

    /**
     * Removes an externally-added attachment from the picker's selection state.
     *
     * Call this when the user removes an attachment that was added via [addExternalAttachments]
     * (e.g. from the camera) from the message composer, so the picker state stays consistent.
     *
     * @param attachment The attachment to remove.
     */
    public fun removeExternalAttachment(attachment: Attachment) {
        _externalAttachments.update { it - attachment }
        persistExternalAttachments(_externalAttachments.value)
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
     * Removes all selected URIs and externally-added attachments. Call this when the associated
     * attachments are consumed (e.g. message sent, poll created) so the picker starts fresh on
     * next open.
     */
    public fun clearSelection() {
        _selectedUris.value = linkedSetOf()
        _selectedGridAttachments.value = emptyMap()
        _externalAttachments.value = emptyList()
        savedStateHandle.remove<Bundle>(KeyExternalAttachments)
        savedStateHandle.remove<Bundle>(KeySelectedGridAttachments)
        savedStateHandle.remove<ArrayList<Uri>>(KeySelectedUris)
    }

    private fun persistExternalAttachments(attachments: List<Attachment>) {
        savedStateHandle[KeyExternalAttachments] = Bundle().apply {
            putParcelableArrayList(KeyExternalAttachmentItems, ArrayList(attachments.map(Attachment::toBundle)))
        }
    }

    private fun persistSelectedGridAttachments(attachments: Map<Uri, Attachment>) {
        savedStateHandle[KeySelectedGridAttachments] = Bundle().apply {
            val bundleList = ArrayList(attachments.values.map(Attachment::toBundle))
            putParcelableArrayList(KeySelectedGridAttachmentItems, bundleList)
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
private const val KeySelectedUris = "stream_selected_uris"
private const val KeyExternalAttachments = "stream_external_attachments"
private const val KeyExternalAttachmentItems = "stream_external_attachment_items"
private const val KeySelectedGridAttachments = "stream_selected_grid_attachments"
private const val KeySelectedGridAttachmentItems = "stream_selected_grid_attachment_items"
private const val KeyBundleUri = "uri"
private const val KeyBundleType = "type"
private const val KeyBundleName = "name"
private const val KeyBundleFileSize = "fileSize"
private const val KeyBundleMimeType = "mimeType"
private const val AttachmentBundleSize = 5

private fun Attachment.toBundle(): Bundle = Bundle(AttachmentBundleSize).apply {
    (extraData[EXTRA_SOURCE_URI] as? String)?.let { putString(KeyBundleUri, it) }
    type?.let { putString(KeyBundleType, it) }
    putString(KeyBundleName, name)
    putInt(KeyBundleFileSize, fileSize)
    mimeType?.let { putString(KeyBundleMimeType, it) }
}

private fun Bundle.toAttachment(): Attachment? {
    val uri = getString(KeyBundleUri) ?: return null
    return Attachment(
        type = getString(KeyBundleType),
        name = getString(KeyBundleName) ?: "",
        fileSize = getInt(KeyBundleFileSize),
        mimeType = getString(KeyBundleMimeType),
        extraData = mapOf(EXTRA_SOURCE_URI to uri),
    )
}

private fun Bundle.toUriAttachmentPair(): Pair<Uri, Attachment>? {
    val attachment = toAttachment() ?: return null
    val uriString = attachment.extraData[EXTRA_SOURCE_URI] as? String ?: return null
    return Uri.parse(uriString) to attachment
}

@Suppress("DEPRECATION")
private fun Bundle.getBundleList(key: String): List<Bundle> =
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        getParcelableArrayList(key, Bundle::class.java) ?: emptyList()
    } else {
        getParcelableArrayList<Bundle>(key) ?: emptyList()
    }

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
