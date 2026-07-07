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

package io.getstream.chat.android.ui.common.feature.messages.composer.mention

import io.getstream.chat.android.models.User
import io.getstream.chat.android.randomString
import io.getstream.chat.android.randomUser
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

@OptIn(ExperimentalCoroutinesApi::class)
internal class CompatUserLookupHandlerTest {

    @Test
    fun `toUserLookupHandler delivers users from the compat callback`() = runTest {
        val query = randomString()
        val users = listOf(randomUser(), randomUser())
        var receivedQuery: String? = null
        val compatHandler = CompatUserLookupHandler { compatQuery, callback ->
            receivedQuery = compatQuery
            callback(users)
            return@CompatUserLookupHandler {}
        }

        val result = compatHandler.toUserLookupHandler().handleUserLookup(query)

        assertEquals(query, receivedQuery)
        assertEquals(users, result)
    }

    @Test
    fun `toUserLookupHandler suspends until the compat callback is invoked`() = runTest {
        val users = listOf(randomUser())
        val compatHandler = CompatUserLookupHandler { _, callback ->
            val deliveryJob = launch {
                delay(100)
                callback(users)
            }
            return@CompatUserLookupHandler { deliveryJob.cancel() }
        }

        val result = compatHandler.toUserLookupHandler().handleUserLookup(randomString())

        assertEquals(users, result)
    }

    @Test
    fun `toUserLookupHandler invokes the cancel function when the lookup is cancelled`() = runTest {
        var cancelCount = 0
        val compatHandler = CompatUserLookupHandler { _, _ ->
            return@CompatUserLookupHandler { cancelCount++ }
        }

        val lookupJob = launch {
            compatHandler.toUserLookupHandler().handleUserLookup(randomString())
        }
        runCurrent()
        lookupJob.cancel()
        runCurrent()

        assertEquals(1, cancelCount)
    }

    @Test
    fun `toJavaCompatUserLookupHandler delivers users to the callback`() {
        val query = randomString()
        val users = listOf(randomUser(), randomUser())
        var receivedQuery: String? = null
        val handler = UserLookupHandler { lookupQuery ->
            receivedQuery = lookupQuery
            users
        }

        var receivedUsers: List<User>? = null
        val cancel = handler.toJavaCompatUserLookupHandler().handleCompatUserLookup(query) { receivedUsers = it }

        assertEquals(query, receivedQuery)
        assertEquals(users, receivedUsers)
        // The returned cancel function is a no-op and must not throw.
        cancel()
    }
}
