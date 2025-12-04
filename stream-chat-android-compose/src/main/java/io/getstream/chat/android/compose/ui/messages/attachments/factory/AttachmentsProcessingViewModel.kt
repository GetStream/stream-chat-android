/*
 * Copyright (c) 2014-2025 Stream.io Inc. All rights reserved.
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

package io.getstream.chat.android.compose.ui.messages.attachments.factory

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import io.getstream.chat.android.compose.ui.util.StorageHelperWrapper
import io.getstream.chat.android.core.internal.coroutines.DispatcherProvider
import io.getstream.chat.android.ui.common.state.messages.composer.AttachmentMetaData
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch

/**
 * Internal ViewModel responsible for asynchronous processing of attachment metadata.
 *
 * This ViewModel handles the background retrieval and processing of attachment metadata from various
 * sources (URIs, files, media) without blocking the main thread. It uses [StorageHelperWrapper] to
 * interact with the device's storage and emits results through [SharedFlow]s that the UI can collect.
 *
 * All processing operations run on [DispatcherProvider.IO] to avoid blocking the main thread during
 * disk I/O operations. The ViewModel provides three main capabilities:
 *
 * 1. **URI Processing**: Converts a list of URIs to attachment metadata via [getAttachmentsMetadataFromUrisAsync]
 * 2. **File Retrieval**: Fetches file metadata from storage via [getFilesAsync]
 * 3. **Media Retrieval**: Fetches media metadata from storage via [getMediaAsync]
 *
 * ## Threading Model
 * All operations are launched in the [viewModelScope] with [DispatcherProvider.IO] to ensure:
 * - Non-blocking execution on the main thread
 * - Automatic cancellation when the ViewModel is cleared
 * - Sequential emission of results through SharedFlows
 *
 * ## Usage
 * This ViewModel is typically used within the message composer to handle attachment selection
 * and processing:
 *
 * ```kotlin
 * val viewModel = viewModel<AttachmentsProcessingViewModel>(
 *     factory = AttachmentsProcessingViewModelFactory(storageHelper)
 * )
 *
 * // Collect metadata updates
 * LaunchedEffect(Unit) {
 *     viewModel.attachmentsMetadataFromUris.collect { result ->
 *         // Handle attachment metadata
 *     }
 * }
 *
 * // Trigger processing
 * viewModel.getAttachmentsMetadataFromUrisAsync(selectedUris)
 * ```
 *
 * @param storageHelper The wrapper around storage helper functionality used to retrieve attachment
 * metadata from the device's storage system.
 *
 * @see AttachmentsMetadataFromUris
 * @see AttachmentsProcessingViewModelFactory
 */
internal class AttachmentsProcessingViewModel(
    private val storageHelper: StorageHelperWrapper,
) : ViewModel() {

    private val _attachmentsMetadataFromUris =
        MutableSharedFlow<AttachmentsMetadataFromUris>(extraBufferCapacity = 1)
    private val _filesMetadata =
        MutableSharedFlow<List<AttachmentMetaData>>(extraBufferCapacity = 1)
    private val _mediaMetadata =
        MutableSharedFlow<List<AttachmentMetaData>>(extraBufferCapacity = 1)

    /**
     * Flow of events emitted when attachments metadata is retrieved from URIs.
     *
     * This [SharedFlow] emits [AttachmentsMetadataFromUris] events that contain both the original URIs
     * and the retrieved [AttachmentMetaData]. The UI can collect from this flow to react to metadata
     * retrieval and update the attachment state accordingly.
     */
    val attachmentsMetadataFromUris: SharedFlow<AttachmentsMetadataFromUris> =
        _attachmentsMetadataFromUris.asSharedFlow()

    /**
     * Flow of events emitted when files metadata is retrieved.
     *
     * This [SharedFlow] emits lists of [AttachmentMetaData] representing the files retrieved from storage.
     * The UI can collect from this flow to react to file metadata retrieval and update the attachment state
     * accordingly.
     */
    val filesMetadata: SharedFlow<List<AttachmentMetaData>> =
        _filesMetadata.asSharedFlow()

    /**
     * Flow of events emitted when media metadata is retrieved.
     *
     * This [SharedFlow] emits lists of [AttachmentMetaData] representing the media retrieved from storage.
     * The UI can collect from this flow to react to media metadata retrieval and update the attachment state
     * accordingly.
     */
    val mediaMetadata: SharedFlow<List<AttachmentMetaData>> =
        _mediaMetadata.asSharedFlow()

    /**
     * Processes a list of attachment URIs in the background and emits the result.
     *
     * This method launches a coroutine on [DispatcherProvider.IO] to perform disk I/O operations without
     * blocking the main thread. Once processing completes, it emits an [AttachmentsMetadataFromUris]
     * event through the [attachmentsMetadataFromUris] flow.
     *
     * The processing is fire-and-forget; multiple calls to this method will queue up separate
     * processing jobs that execute independently. Each job runs in the [viewModelScope] and will
     * be cancelled automatically if the ViewModel is cleared before completion.
     *
     * ## Threading
     * - **Caller thread**: Any (method returns immediately)
     * - **Execution thread**: [DispatcherProvider.IO] (coroutine context)
     * - **Emission thread**: Depends on the collector's coroutine context
     *
     * @param uris The list of URIs to process. Can be empty, in which case an empty result is emitted.
     */
    fun getAttachmentsMetadataFromUrisAsync(uris: List<Uri>) {
        viewModelScope.launch(DispatcherProvider.IO) {
            val metadata = storageHelper.getAttachmentsMetadataFromUris(uris)
            val attachmentsMetadataFromUris = AttachmentsMetadataFromUris(
                uris = uris,
                attachmentsMetadata = metadata,
            )
            _attachmentsMetadataFromUris.emit(attachmentsMetadataFromUris)
        }
    }

    /**
     * Retrieves files metadata asynchronously and emits the result.
     *
     * This method launches a coroutine on [DispatcherProvider.IO] to perform disk I/O operations without
     * blocking the main thread. Once retrieval completes, it emits a list of [AttachmentMetaData]
     * through the [filesMetadata] flow.
     *
     * The retrieval is fire-and-forget; multiple calls to this method will queue up separate
     * retrieval jobs that execute independently. Each job runs in the [viewModelScope] and will
     * be cancelled automatically if the ViewModel is cleared before completion.
     *
     * ## Threading
     * - **Caller thread**: Any (method returns immediately)
     * - **Execution thread**: [DispatcherProvider.IO] (coroutine context)
     * - **Emission thread**: Depends on the collector's coroutine context
     */
    fun getFilesAsync() {
        viewModelScope.launch(DispatcherProvider.IO) {
            val metadata = storageHelper.getFiles()
            _filesMetadata.emit(metadata)
        }
    }

    /**
     * Retrieves media metadata asynchronously and emits the result.
     *
     * This method launches a coroutine on [DispatcherProvider.IO] to perform disk I/O operations without
     * blocking the main thread. Once retrieval completes, it emits a list of [AttachmentMetaData]
     * through the [mediaMetadata] flow.
     *
     * The retrieval is fire-and-forget; multiple calls to this method will queue up separate
     * retrieval jobs that execute independently. Each job runs in the [viewModelScope] and will
     * be cancelled automatically if the ViewModel is cleared before completion.
     *
     * ## Threading
     * - **Caller thread**: Any (method returns immediately)
     * - **Execution thread**: [DispatcherProvider.IO] (coroutine context)
     * - **Emission thread**: Depends on the collector's coroutine context
     */
    fun getMediaAsync() {
        viewModelScope.launch(DispatcherProvider.IO) {
            val metadata = storageHelper.getMedia()
            _mediaMetadata.emit(metadata)
        }
    }
}

/**
 * Data class representing the result of processing attachment URIs into metadata.
 *
 * This class pairs the original list of [Uri]s with their corresponding [AttachmentMetaData] that
 * was retrieved from storage. It's emitted through [AttachmentsProcessingViewModel.attachmentsMetadataFromUris]
 * after the async processing completes.
 *
 * The presence of both the original URIs and the metadata allows consumers to:
 * - Match results back to the original request
 * - Handle cases where some URIs may not produce valid metadata
 * - Track processing progress across multiple async operations
 *
 * @property uris The original list of URIs that were submitted for processing. This list maintains
 * the order in which URIs were provided to [AttachmentsProcessingViewModel.getAttachmentsMetadataFromUrisAsync].
 * @property attachmentsMetadata The list of successfully retrieved attachment metadata. May contain
 * fewer entries than [uris] if some URIs could not be processed.
 *
 * @see AttachmentsProcessingViewModel.attachmentsMetadataFromUris
 * @see AttachmentsProcessingViewModel.getAttachmentsMetadataFromUrisAsync
 */
internal data class AttachmentsMetadataFromUris(
    val uris: List<Uri>,
    val attachmentsMetadata: List<AttachmentMetaData>,
)

/**
 * A [ViewModelProvider.Factory] for creating [AttachmentsProcessingViewModel] instances.
 *
 * This factory is used to construct [AttachmentsProcessingViewModel] with the required [StorageHelperWrapper]
 * dependency. It ensures that only [AttachmentsProcessingViewModel] instances can be created and throws an
 * [IllegalArgumentException] if an unsupported ViewModel class is requested.
 *
 * @param storageHelper The helper used to access file metadata from storage.
 *
 * @see AttachmentsProcessingViewModel
 */
internal class AttachmentsProcessingViewModelFactory(
    private val storageHelper: StorageHelperWrapper,
) : ViewModelProvider.Factory {

    /**
     * Creates a new instance of the given [ViewModel] class.
     *
     * @param modelClass The class of the ViewModel to create. Must be [AttachmentsProcessingViewModel].
     * @return A new instance of [AttachmentsProcessingViewModel].
     * @throws IllegalArgumentException if [modelClass] is not [AttachmentsProcessingViewModel].
     */
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        require(modelClass == AttachmentsProcessingViewModel::class.java) {
            "AttachmentsProcessingViewModelFactory can only create instances of AttachmentsProcessingViewModel"
        }

        return AttachmentsProcessingViewModel(storageHelper) as T
    }
}
