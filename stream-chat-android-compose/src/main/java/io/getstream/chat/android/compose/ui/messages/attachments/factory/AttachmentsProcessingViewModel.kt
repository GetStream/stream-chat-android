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
 * ViewModel responsible for asynchronous processing of attachment metadata.
 * It handles the background retrieval and processing of attachment metadata from various
 * sources (URIs, files, media) without blocking the main thread.
 *
 * @param storageHelper The wrapper around storage helper functionality used to retrieve attachment
 * metadata from the device's storage system.
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
     */
    val attachmentsMetadataFromUris: SharedFlow<AttachmentsMetadataFromUris> =
        _attachmentsMetadataFromUris.asSharedFlow()

    /**
     * Flow of events emitted when files metadata is retrieved.
     */
    val filesMetadata: SharedFlow<List<AttachmentMetaData>> =
        _filesMetadata.asSharedFlow()

    /**
     * Flow of events emitted when media metadata is retrieved.
     */
    val mediaMetadata: SharedFlow<List<AttachmentMetaData>> =
        _mediaMetadata.asSharedFlow()

    /**
     * Processes a list of attachment URIs in the background and emits the result.
     * Observe the [attachmentsMetadataFromUris] flow to be notified about the result.
     *
     * @param uris The list of URIs to process.
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
     * Observe the [filesMetadata] flow to be notified about the result.
     */
    fun getFilesAsync() {
        viewModelScope.launch(DispatcherProvider.IO) {
            val metadata = storageHelper.getFiles()
            _filesMetadata.emit(metadata)
        }
    }

    /**
     * Retrieves media metadata asynchronously and emits the result.
     * Observe the [mediaMetadata] flow to be notified about the result.
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
 * @property uris The original list of URIs that were submitted for processing.
 * @property attachmentsMetadata The list of successfully retrieved attachment metadata.
 *
 */
internal data class AttachmentsMetadataFromUris(
    val uris: List<Uri>,
    val attachmentsMetadata: List<AttachmentMetaData>,
)

/**
 * A [ViewModelProvider.Factory] for creating [AttachmentsProcessingViewModel] instances.
 *
 * @param storageHelper The helper used to access file metadata from storage.
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
