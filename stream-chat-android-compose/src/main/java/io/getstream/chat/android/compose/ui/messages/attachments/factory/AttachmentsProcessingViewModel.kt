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
import io.getstream.chat.android.ui.common.state.messages.composer.AttachmentMetaData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch

/**
 * ViewModel responsible for processing attachment URIs on a background thread.
 *
 * This ViewModel handles the conversion of URIs to [AttachmentMetaData] by performing
 * disk I/O operations on [Dispatchers.IO] to avoid blocking the main thread and causing ANRs.
 * It processes attachments asynchronously and emits results through a [SharedFlow] that the UI can observe.
 *
 * The processing is triggered by calling [processAttachmentsFromUris], which performs the following:
 * - Reads file metadata from the system storage on a background thread
 * - Converts URIs to [AttachmentMetaData] objects
 * - Emits an [AttachmentsProcessingResult] event with both the original URIs and processed metadata
 *
 * @param storageHelper The helper used to access file metadata from storage.
 *
 * @see AttachmentsProcessingResult
 * @see AttachmentsProcessingViewModelFactory
 */
internal class AttachmentsProcessingViewModel(
    private val storageHelper: StorageHelperWrapper,
) : ViewModel() {

    private val _result = MutableSharedFlow<AttachmentsProcessingResult>(extraBufferCapacity = 1)

    /**
     * Flow of events emitted when attachment processing completes.
     *
     * This [SharedFlow] emits [AttachmentsProcessingResult] events that contain both the original URIs
     * and the processed [AttachmentMetaData]. The UI can collect from this flow to react to processing
     * completion and update the attachment state accordingly.
     *
     * The flow has a buffer capacity of 1, allowing for one event to be buffered if no collectors are active.
     */
    val result: SharedFlow<AttachmentsProcessingResult> = _result.asSharedFlow()

    /**
     * Processes a list of attachment URIs in the background and emits the result.
     *
     * This method launches a coroutine on [Dispatchers.IO] to perform disk I/O operations without
     * blocking the main thread. Once processing completes, it emits an [AttachmentsProcessingResult]
     * event through the [result] flow.
     *
     * The processing is fire-and-forget; multiple calls to this method will queue up separate
     * processing jobs that execute independently.
     *
     * @param uris The list of URIs to process. Can be empty, in which case an empty result is emitted.
     */
    fun processAttachmentsFromUris(uris: List<Uri>) {
        viewModelScope.launch(Dispatchers.IO) {
            val metadata = storageHelper.getAttachmentsMetadataFromUris(uris)
            val attachmentsProcessingResult = AttachmentsProcessingResult(
                uris = uris,
                processedAttachments = metadata,
            )
            _result.emit(attachmentsProcessingResult)
        }
    }
}

/**
 * Result of attachment URI processing, containing both the original URIs and the processed metadata.
 *
 * This data class is emitted by [AttachmentsProcessingViewModel] when attachment processing completes.
 * It pairs the original URIs with their corresponding [AttachmentMetaData], allowing consumers to
 * correlate the input with the output and handle the processed attachments appropriately.
 *
 * @property uris The original list of URIs that were submitted for processing.
 * @property processedAttachments The list of processed [AttachmentMetaData] extracted from the URIs.
 *                                May be smaller than [uris] if some URIs could not be processed or
 *                                were filtered out by attachment filters.
 *
 * @see AttachmentsProcessingViewModel
 */
internal data class AttachmentsProcessingResult(
    val uris: List<Uri>,
    val processedAttachments: List<AttachmentMetaData>,
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
