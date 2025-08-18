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
import io.getstream.chat.android.models.User
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn

class UserProfileViewModel(
    private val chatClient: ChatClient = ChatClient.instance(),
) : ViewModel() {

    val user: StateFlow<User?> =
        chatClient.clientState.user
            .flatMapLatest(::queryUser)
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(StopTimeout),
                initialValue = null,
            )

    /**
     * Query updated user information from the server.
     */
    private fun queryUser(user: User?) = flow {
        if (user == null) {
            emit(null)
        } else {
            val filter = Filters.eq("id", user.id)
            val request = QueryUsersRequest(filter, offset = 0, limit = 1)
            val result = chatClient.queryUsers(request).await()
            emit(result.getOrNull()?.firstOrNull())
        }
    }
}

private const val StopTimeout = 5000L
