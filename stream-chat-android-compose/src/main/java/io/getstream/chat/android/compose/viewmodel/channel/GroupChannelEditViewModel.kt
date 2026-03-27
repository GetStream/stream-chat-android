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

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import io.getstream.chat.android.client.ChatClient
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
 * Handles uploading a new channel image and/or updating the channel name in a single save action.
 *
 * @param cid The full channel identifier (e.g., "messaging:123").
 * @param chatClient The [ChatClient] instance used for API calls.
 */
internal class GroupChannelEditViewModel(
    cid: String,
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
     * Persists channel edits to the backend.
     *
     * When [imageFile] is provided, the image is uploaded first and the resulting URL is included in
     * the channel update together with [name]. When [removeImage] is true, the channel image is
     * cleared. Both name and image changes are sent in a single `updatePartial` call.
     *
     * @param name The new channel name.
     * @param imageFile An optional image file to upload as the new channel avatar.
     * @param removeImage Whether to remove the current channel image.
     */
    fun save(name: String, imageFile: File?, removeImage: Boolean) {
        if (_state.value.isSaving) return
        logger.d { "[save] name: $name, imageFile: ${imageFile?.name}, removeImage: $removeImage" }
        _state.update { it.copy(isSaving = true) }
        viewModelScope.launch {
            val imageUrl = resolveImageUrl(imageFile, removeImage)
            if (imageUrl is ImageUrlResult.Error) {
                emitResult(GroupChannelEditViewEvent.SaveError)
                return@launch
            }
            val updates = buildUpdates(name, imageUrl)
            val result = channelClient.updatePartial(set = updates).await()
            val event = if (result.isSuccess) {
                GroupChannelEditViewEvent.SaveSuccess
            } else {
                logger.e { "[save] updatePartial failed: ${result.errorOrNull()?.message}" }
                GroupChannelEditViewEvent.SaveError
            }
            emitResult(event)
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

    private fun emitResult(event: GroupChannelEditViewEvent) {
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
 */
internal data class GroupChannelEditViewState(
    val isSaving: Boolean = false,
)

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
 * @param cid The full channel identifier (e.g., "messaging:123").
 */
internal class GroupChannelEditViewModelFactory(
    private val cid: String,
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        @Suppress("UNCHECKED_CAST")
        return GroupChannelEditViewModel(cid = cid) as T
    }
}
