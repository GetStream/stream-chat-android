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

package io.getstream.chat.android.client.scope.user

import io.getstream.chat.android.models.User
import io.getstream.chat.android.models.UserId
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withTimeout
import kotlin.coroutines.AbstractCoroutineContextElement
import kotlin.coroutines.CoroutineContext

/**
 * The [CoroutineContext.Element] which holds the current [UserId].
 */
internal class UserIdentifier : AbstractCoroutineContextElement(UserIdentifier) {

    private val _value = MutableStateFlow<UserId?>(null)

    /**
     * Key for [UserIdentifier] instance in the coroutine context.
     */
    companion object Key : CoroutineContext.Key<UserIdentifier> {
        private const val DEFAULT_TIMEOUT_IN_MS = 10_000L
    }

    /**
     * Represents [User.id] String
     */
    var value: UserId?
        get() = _value.value
        set(value) {
            _value.value = value
        }

    /**
     * Awaits for the specified [userId] being set.
     *
     * @param userId Required user_id to wait for.
     * @param timeoutInMs A timeout in milliseconds when the process will be cancelled.
     */
    suspend fun awaitFor(userId: UserId, timeoutInMs: Long = DEFAULT_TIMEOUT_IN_MS) = runCatching {
        withTimeout(timeoutInMs) {
            _value.first { it == userId }
        }
    }

    /**
     * Returns a string representation of the object.
     */
    override fun toString(): String = "UserIdentifier($value)"
}
