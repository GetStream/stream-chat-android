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

package io.getstream.chat.android.compose.viewmodel.channel

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.internal.file.StreamFileManager
import io.getstream.log.taggedLogger
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.File

/**
 * ViewModel responsible for managing the group channel edit screen state and save operations.
 *
 * Handles copying a gallery [Uri] to cache, uploading a new channel image, and/or updating the
 * channel name in a single save action.
 *
 * @param cid The full channel identifier (e.g., "messaging:123").
 * @param galleryImageCopier Copies a picked [Uri] to a local cache [File].
 * @param chatClient The [ChatClient] instance used for API calls.
 */
internal class GroupChannelEditViewModel(
    private val cid: String,
    private val galleryImageCopier: GalleryImageCopier,
    private val chatClient: ChatClient = ChatClient.instance(),
) : ViewModel() {

    private val logger by taggedLogger("Chat:GroupChannelEditVM")
    private val channelClient = chatClient.channel(cid)

    private val _state = MutableStateFlow(GroupChannelEditViewState())

    /** The current state of the edit screen. */
    val state: StateFlow<GroupChannelEditViewState> = _state.asStateFlow()

    private val _events = MutableSharedFlow<GroupChannelEditViewEvent>(extraBufferCapacity = 1)

    /** One-shot events emitted by the ViewModel. */
    val events: SharedFlow<GroupChannelEditViewEvent> = _events.asSharedFlow()

    /**
     * Copies a gallery [Uri] to app cache via [GalleryImageCopier].
     * Sets [GroupChannelEditViewState.isImporting] for the duration of the copy;
     * [save] is ignored while that flag is true. On success the resulting [File] is stored in
     * [GroupChannelEditViewState.pendingImageFile].
     *
     * @param uri The content [Uri] returned by the gallery picker.
     */
    fun importGalleryImage(uri: Uri) {
        if (_state.value.isImporting) return
        _state.update { it.copy(isImporting = true) }
        viewModelScope.launch {
            val file = galleryImageCopier.copyToCache(uri)
            _state.update {
                it.copy(
                    isImporting = false,
                    pendingImageFile = file ?: it.pendingImageFile,
                    removeImage = if (file != null) false else it.removeImage,
                )
            }
        }
    }

    /**
     * Stores an image file as the pending channel avatar.
     *
     * @param file The local image file to use (e.g. from camera capture).
     */
    fun setPendingImage(file: File) {
        _state.update { it.copy(pendingImageFile = file, removeImage = false) }
    }

    /**
     * Marks the current channel image for removal and clears any pending image.
     */
    fun removeImage() {
        _state.update { it.copy(pendingImageFile = null, removeImage = true) }
    }

    /**
     * Persists channel edits to the backend.
     *
     * Reads [GroupChannelEditViewState.pendingImageFile] and [GroupChannelEditViewState.removeImage]
     * from the current state snapshot. When a pending image exists it is uploaded first; when
     * [GroupChannelEditViewState.removeImage] is true the channel image is cleared. Both name and
     * image changes are sent in a single `updatePartial` call.
     *
     * Ignored while [GroupChannelEditViewState.isBusy] is true.
     *
     * @param name The new channel name.
     */
    fun save(name: String) {
        val trimmedName = name.trim()
        val snapshot = _state.value
        if (snapshot.isBusy) return
        val imageFile = snapshot.pendingImageFile
        val removeImage = snapshot.removeImage
        logger.d { "[save] name: $trimmedName, imageFile: ${imageFile?.name}, removeImage: $removeImage" }
        _state.update { it.copy(isSaving = true) }
        viewModelScope.launch {
            val imageUrl = resolveImageUrl(imageFile, removeImage)
            if (imageUrl is ImageUrlResult.Error) {
                emitSaveResult(GroupChannelEditViewEvent.SaveError)
                return@launch
            }
            val updates = buildUpdates(trimmedName, imageUrl)
            val result = channelClient.updatePartial(set = updates).await()
            val event = if (result.isSuccess) {
                GroupChannelEditViewEvent.SaveSuccess
            } else {
                logger.e { "[save] updatePartial failed: ${result.errorOrNull()?.message}" }
                GroupChannelEditViewEvent.SaveError
            }
            emitSaveResult(event)
        }
    }

    private suspend fun resolveImageUrl(imageFile: File?, removeImage: Boolean): ImageUrlResult = when {
        imageFile != null -> {
            val result = chatClient.uploadImage(imageFile).await()
            val url = result.getOrNull()?.file
            if (url != null) {
                ImageUrlResult.Url(url)
            } else {
                logger.e { "[resolveImageUrl] upload failed: ${result.errorOrNull()?.message}" }
                ImageUrlResult.Error
            }
        }
        removeImage -> ImageUrlResult.Url("")
        else -> ImageUrlResult.NoChange
    }

    private fun buildUpdates(name: String, imageUrl: ImageUrlResult): Map<String, Any> = buildMap {
        put("name", name)
        if (imageUrl is ImageUrlResult.Url) put("image", imageUrl.value)
    }

    private fun emitSaveResult(event: GroupChannelEditViewEvent) {
        _state.update { it.copy(isSaving = false) }
        _events.tryEmit(event)
    }
}

private sealed interface ImageUrlResult {
    data class Url(val value: String) : ImageUrlResult
    data object NoChange : ImageUrlResult
    data object Error : ImageUrlResult
}

/**
 * Represents the UI state of the group channel edit screen.
 *
 * @param isSaving Whether a save operation is currently in progress.
 * @param isImporting Whether the picked image is being copied from the picker to app cache (see
 * [GroupChannelEditViewModel.importGalleryImage]).
 * @param pendingImageFile A locally cached image file selected as the new channel avatar, or `null`
 * if no new image has been picked.
 * @param removeImage Whether the current channel image should be removed on save.
 */
internal data class GroupChannelEditViewState(
    val isSaving: Boolean = false,
    val isImporting: Boolean = false,
    val pendingImageFile: File? = null,
    val removeImage: Boolean = false,
) {
    /** True while the user cannot safely save (save in flight or image import in progress). */
    val isBusy: Boolean
        get() = isSaving || isImporting
}

/** One-shot events emitted by [GroupChannelEditViewModel]. */
internal sealed interface GroupChannelEditViewEvent {
    /** The save operation completed successfully. */
    data object SaveSuccess : GroupChannelEditViewEvent

    /** The save operation failed. */
    data object SaveError : GroupChannelEditViewEvent
}

/**
 * Factory for creating [GroupChannelEditViewModel] instances.
 *
 * @param context A [Context] used for gallery import.
 * @param cid The full channel identifier (e.g., "messaging:123").
 * @param fileManager Optional cache writer; defaults to a new [StreamFileManager].
 */
internal class GroupChannelEditViewModelFactory(
    private val context: Context,
    private val cid: String,
    private val fileManager: StreamFileManager = StreamFileManager(),
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        @Suppress("UNCHECKED_CAST")
        return GroupChannelEditViewModel(
            cid = cid,
            galleryImageCopier = ContentResolverImageCopier(
                context = context.applicationContext,
                fileManager = fileManager,
            ),
        ) as T
    }
}
