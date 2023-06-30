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

package io.getstream.chat.android.offline.plugin.listener.internal

import io.getstream.chat.android.client.models.User
import io.getstream.chat.android.client.plugin.listeners.FetchCurrentUserListener
import io.getstream.chat.android.client.utils.Result
import io.getstream.chat.android.offline.event.handler.internal.model.SelfUserFull
import io.getstream.chat.android.offline.event.handler.internal.utils.updateCurrentUser
import io.getstream.chat.android.offline.plugin.state.global.internal.GlobalMutableState
import io.getstream.logging.StreamLog

internal class FetchCurrentUserListenerState(
    private val globalMutableState: GlobalMutableState,
) : FetchCurrentUserListener {

    private val logger = StreamLog.getLogger("Chat:FetchCurUserLST")

    override suspend fun onFetchCurrentUserResult(result: Result<User>) {
        if (result.isSuccess) {
            logger.d { "[onFetchCurrentUserResult] result: $result" }
            globalMutableState.updateCurrentUser(SelfUserFull(result.data()))
        }
    }
}
