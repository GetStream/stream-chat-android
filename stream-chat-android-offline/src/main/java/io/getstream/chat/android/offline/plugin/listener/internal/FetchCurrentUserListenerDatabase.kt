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

package io.getstream.chat.android.offline.plugin.listener.internal

import io.getstream.chat.android.client.persistance.repository.UserRepository
import io.getstream.chat.android.client.plugin.listeners.FetchCurrentUserListener
import io.getstream.chat.android.models.User
import io.getstream.log.taggedLogger
import io.getstream.result.Result
import io.getstream.result.onSuccessSuspend

internal class FetchCurrentUserListenerDatabase(
    private val userRepository: UserRepository,
) : FetchCurrentUserListener {

    private val logger by taggedLogger("Chat:FetchCurUserLDB")

    override suspend fun onFetchCurrentUserResult(result: Result<User>) {
        result.onSuccessSuspend {
            logger.d { "[onFetchCurrentUserResult] result: $result" }
            userRepository.insertCurrentUser(it)
        }
    }
}
