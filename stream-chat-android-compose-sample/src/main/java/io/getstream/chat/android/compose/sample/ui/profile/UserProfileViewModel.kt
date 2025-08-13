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
import io.getstream.chat.android.models.Filters
import io.getstream.chat.android.models.UnreadCounts
import io.getstream.chat.android.models.User
import io.getstream.chat.android.models.UserId
import io.getstream.result.Error
import io.getstream.result.onSuccessSuspend
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class UserProfileViewModel(
    private val chatClient: ChatClient = ChatClient.instance(),
) : ViewModel() {

    private val _unreadCounts = MutableStateFlow<UnreadCounts?>(null)

    val state: StateFlow<UserProfileViewState> =
        combine(
            queryUser(userId = chatClient.getCurrentUser()?.id),
            _unreadCounts,
        ) { user, unreadCounts ->
            UserProfileViewState(
                user = user,
                unreadCounts = unreadCounts,
            )
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(StopTimeout),
            initialValue = UserProfileViewState(),
        )

    private val _errors = MutableSharedFlow<Error>(extraBufferCapacity = 1)
    val errors: SharedFlow<Error> = _errors.asSharedFlow()

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
                .onSuccessSuspend { emit(it.firstOrNull()) }
                .onError(_errors::tryEmit)
        }
    }

    fun loadUnreadCounts() {
        _unreadCounts.value = null
        viewModelScope.launch {
            chatClient.getUnreadCounts()
                .await()
                .onSuccess { _unreadCounts.value = it }
                .onError(_errors::tryEmit)
        }
    }
}

private const val StopTimeout = 5000L
