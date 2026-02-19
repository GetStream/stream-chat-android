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
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import io.getstream.chat.android.core.internal.coroutines.DispatcherProvider
import io.getstream.chat.android.ui.common.helper.internal.AttachmentStorageHelper
import io.getstream.chat.android.ui.common.state.messages.composer.AttachmentMetaData
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * ViewModel responsible for asynchronous processing of attachment metadata.
 * It handles the background retrieval and processing of attachment metadata from various
 * sources (URIs, files, media) without blocking the main thread.
 *
 * @param storageHelper Retrieves attachment metadata from device storage.
 */
internal class AttachmentProcessingViewModel(
    private val storageHelper: AttachmentStorageHelper,
) : ViewModel() {

    /**
     * Processes a list of attachment URIs in the background and emits the result.
     *
     * @param uris The list of URIs to process.
     * @param onComplete The callback passing the processed attachments.
     */
    fun getAttachmentsMetadataFromUrisAsync(uris: List<Uri>, onComplete: (AttachmentsMetadataFromUris) -> Unit) {
        viewModelScope.launch {
            val attachmentsMetadataFromUris = withContext(DispatcherProvider.IO) {
                val metadata = storageHelper.resolveMetadata(uris)
                AttachmentsMetadataFromUris(
                    uris = uris,
                    attachmentsMetadata = metadata,
                )
            }
            onComplete(attachmentsMetadataFromUris)
        }
    }

    /**
     * Retrieves files metadata asynchronously and emits the result.
     *
     * @param onComplete The callback passing the resolved files.
     */
    fun getFilesAsync(onComplete: (List<AttachmentMetaData>) -> Unit) {
        viewModelScope.launch {
            val metadata = withContext(DispatcherProvider.IO) {
                storageHelper.getFileMetadata()
            }
            onComplete(metadata)
        }
    }

    /**
     * Retrieves media metadata asynchronously and emits the result.
     *
     * @param onComplete The callback passing the resolved files.
     */
    fun getMediaAsync(onComplete: (List<AttachmentMetaData>) -> Unit) {
        viewModelScope.launch {
            val metadata = withContext(DispatcherProvider.IO) {
                storageHelper.getMediaMetadata()
            }
            onComplete(metadata)
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
 * A [androidx.lifecycle.ViewModelProvider.Factory] for creating [AttachmentProcessingViewModel] instances.
 *
 * @param storageHelper The helper used to access file metadata from storage.
 */
internal class AttachmentProcessingViewModelFactory(
    private val storageHelper: AttachmentStorageHelper,
) : ViewModelProvider.Factory {

    /**
     * Creates a new instance of the given [ViewModel] class.
     *
     * @param modelClass The class of the ViewModel to create. Must be [AttachmentProcessingViewModel].
     * @return A new instance of [AttachmentProcessingViewModel].
     * @throws IllegalArgumentException if [modelClass] is not [AttachmentProcessingViewModel].
     */
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        require(modelClass == AttachmentProcessingViewModel::class.java) {
            "AttachmentProcessingViewModelFactory can only create instances of AttachmentProcessingViewModel"
        }

        return AttachmentProcessingViewModel(storageHelper) as T
    }
}
