/*
 * Copyright (c) 2014-2023 Stream.io Inc. All rights reserved.
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

package io.getstream.chat.android.state.plugin.listener.internal

import io.getstream.chat.android.client.plugin.listeners.FetchCurrentUserListener
import io.getstream.chat.android.client.setup.state.ClientState
import io.getstream.chat.android.models.User
import io.getstream.chat.android.state.event.handler.internal.model.SelfUserFull
import io.getstream.chat.android.state.event.handler.internal.utils.updateCurrentUser
import io.getstream.chat.android.state.plugin.state.global.internal.MutableGlobalState
import io.getstream.log.taggedLogger
import io.getstream.result.Result

internal class FetchCurrentUserListenerState(
    private val clientState: ClientState,
    private val globalMutableState: MutableGlobalState,
) : FetchCurrentUserListener {

    private val logger by taggedLogger("Chat:FetchCurUserLST")

    override suspend fun onFetchCurrentUserResult(result: Result<User>) {
        if (result.isSuccess) {
            logger.d { "[onFetchCurrentUserResult] result: $result" }
            globalMutableState.updateCurrentUser(
                currentUser = clientState.user.value,
                receivedUser = SelfUserFull(result.getOrThrow()),
            )
        }
    }
}
