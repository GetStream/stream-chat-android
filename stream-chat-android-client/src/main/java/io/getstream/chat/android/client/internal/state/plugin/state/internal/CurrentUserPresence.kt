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

package io.getstream.chat.android.client.internal.state.plugin.state.internal

import io.getstream.chat.android.client.internal.state.utils.internal.combineStates
import io.getstream.chat.android.models.ConnectionState
import io.getstream.chat.android.models.User
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.filterNot
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

/**
 * Overlays the current user's presence on the latest users, from the connection rather than from the cached copy.
 *
 * The server sends no presence events for the connected user's own record, and the database does not store presence,
 * so the cached copy of the current user keeps whatever presence it was last loaded with. A reconnect in progress
 * keeps the previous presence, so a short network blip does not flip the current user offline.
 */
internal fun StateFlow<Map<String, User>>.withCurrentUserPresence(
    currentUser: StateFlow<User?>,
    connectionState: StateFlow<ConnectionState>,
    scope: CoroutineScope,
): StateFlow<Map<String, User>> {
    val connected = connectionState
        .filterNot { it == ConnectionState.Connecting }
        .map { it == ConnectionState.Connected }
        .stateIn(scope, SharingStarted.Eagerly, connectionState.value == ConnectionState.Connected)
    return combineStates(this, currentUser, connected) { users, user, isConnected ->
        users.withCurrentUserPresence(user, isConnected)
    }
}

private fun Map<String, User>.withCurrentUserPresence(currentUser: User?, connected: Boolean): Map<String, User> {
    currentUser ?: return this
    val cachedUser = this[currentUser.id] ?: currentUser
    // The cached copy may carry an invisibility toggled during the session, which the current user misses.
    val online = connected && currentUser.invisible != true && cachedUser.invisible != true
    return if (cachedUser.online == online && currentUser.id in this) {
        this
    } else {
        this + (currentUser.id to cachedUser.copy(online = online))
    }
}
