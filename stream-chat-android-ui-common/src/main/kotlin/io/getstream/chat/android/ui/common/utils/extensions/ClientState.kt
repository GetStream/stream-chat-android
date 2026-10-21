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

package io.getstream.chat.android.ui.common.utils.extensions

import io.getstream.chat.android.client.setup.state.ClientState
import io.getstream.chat.android.core.internal.InternalStreamChatApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map

/**
 * Emits each time the connected user's session ends, which makes data cached for that user stale: on disconnect, or
 * when a different user replaces them. A first connect, the same user connecting again and profile updates emit
 * nothing.
 *
 * A switch emits as soon as the new user is set, before the client finishes initializing for them.
 */
@InternalStreamChatApi
public fun ClientState.userSessionEnds(): Flow<Unit> = flow {
    var previousUserId: String? = null
    user.map { it?.id }.distinctUntilChanged().collect { userId ->
        if (previousUserId != null) emit(Unit)
        previousUserId = userId
    }
}
