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

package io.getstream.chat.android.compose.sample.ui.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.api.models.QueryUsersRequest
import io.getstream.chat.android.client.utils.ProgressCallback
import io.getstream.chat.android.models.Filters
import io.getstream.chat.android.models.User
import io.getstream.chat.android.models.UserId
import io.getstream.result.Error
import io.getstream.result.onErrorSuspend
import io.getstream.result.onSuccessSuspend
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.File

class UserProfileViewModel(
    private val chatClient: ChatClient = ChatClient.instance(),
) : ViewModel() {

    private val _state = MutableStateFlow(UserProfileViewState())
    val state: StateFlow<UserProfileViewState> = _state.asStateFlow()

    private val _events = MutableSharedFlow<UserProfileViewEvent>(extraBufferCapacity = 1)
    val events: SharedFlow<UserProfileViewEvent> = _events.asSharedFlow()

    init {
        queryUser(userId = chatClient.getCurrentUser()?.id)
            .onEach { user -> _state.update { currentState -> currentState.copy(user = user) } }
            .launchIn(viewModelScope)
    }

    /**
     * Query updated user information from the server.
     */
    private fun queryUser(userId: UserId?): Flow<User?> = flow {
        if (userId == null) {
            emit(null)
        } else {
            val filter = Filters.eq("id", userId)
            val request = QueryUsersRequest(filter, offset = 0, limit = 1)
            chatClient.queryUsers(request)
                .await()
                .onSuccessSuspend { users -> emit(users.firstOrNull()) }
                .onErrorSuspend { error -> _events.emit(UserProfileViewEvent.LoadUserError(error)) }
        }
    }

    fun updateProfilePicture(imageFile: File) {
        fun onError(error: Error) {
            _state.update { currentState ->
                currentState.copy(progressIndicator = null)
            }
            _events.tryEmit(UserProfileViewEvent.UpdateProfilePictureError(error))
        }
        viewModelScope.launch {
            // Upload the user image file
            chatClient.uploadImage(
                file = imageFile,
                progressCallback = object : ProgressCallback {
                    override fun onProgress(bytesUploaded: Long, totalBytes: Long) {
                        val progress = bytesUploaded.toFloat() / totalBytes
                        _state.update { currentState ->
                            currentState.copy(progressIndicator = UserProfileViewState.ProgressIndicator(progress))
                        }
                    }
                },
            ).await()
                .onSuccessSuspend { uploadedFile ->
                    val user = state.value.user!!
                    val url = uploadedFile.file
                    _state.update { currentState ->
                        currentState.copy(progressIndicator = UserProfileViewState.ProgressIndicator())
                    }
                    // Update the user entity with the uploaded image url
                    chatClient.updateUser(user = user.copy(image = url))
                        .await()
                        .onSuccess { updatedUser ->
                            _state.update { currentState ->
                                currentState.copy(
                                    user = updatedUser,
                                    progressIndicator = null,
                                )
                            }
                            _events.tryEmit(UserProfileViewEvent.UpdateProfilePictureSuccess)
                        }
                        .onError(::onError)
                }
                .onError(::onError)
        }
    }

    fun loadUnreadCounts() {
        _state.update { currentState -> currentState.copy(unreadCounts = null) }
        viewModelScope.launch {
            chatClient.getUnreadCounts()
                .await()
                .onSuccess { unreadCounts ->
                    _state.update { currentState -> currentState.copy(unreadCounts = unreadCounts) }
                }
                .onError { error -> _events.tryEmit(UserProfileViewEvent.LoadUnreadCountsError(error)) }
        }
    }

    fun removeProfilePicture() {
        _state.update { currentState ->
            currentState.copy(progressIndicator = UserProfileViewState.ProgressIndicator())
        }
        val user = state.value.user!!
        fun onError(error: Error) {
            _state.update { currentState ->
                currentState.copy(progressIndicator = null)
            }
            _events.tryEmit(UserProfileViewEvent.RemoveProfilePictureError(error))
        }
        viewModelScope.launch {
            chatClient.deleteImage(url = user.image)
                .await()
                .onSuccessSuspend {
                    chatClient.updateUser(user = user.copy(image = ""))
                        .await()
                        .onSuccess { updatedUser ->
                            _state.update { currentState ->
                                currentState.copy(
                                    user = updatedUser,
                                    progressIndicator = null,
                                )
                            }
                        }
                        .onError(::onError)
                }
                .onError(::onError)
        }
    }
}
