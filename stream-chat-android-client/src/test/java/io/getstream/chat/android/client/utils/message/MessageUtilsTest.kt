/*
 * Copyright (c) 2014-2024 Stream.io Inc. All rights reserved.
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

package io.getstream.chat.android.client.utils.message

import io.getstream.chat.android.randomMessage
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.currentTime
import kotlinx.coroutines.test.runTest
import org.amshove.kluent.shouldBeEqualTo
import org.junit.Test
import java.util.Date

@ExperimentalCoroutinesApi
internal class MessageUtilsTest {

    @Test
    fun `validate message_isPinExpired`() = runTest {
        val message = randomMessage(
            pinned = true,
            pinExpires = Date(currentTime + 1000),
        )

        advanceTimeBy(2000)

        message.isPinExpired { currentTime } shouldBeEqualTo true
    }

    @Test
    fun `validate message_isPinned when pin expires`() = runTest {
        val message = randomMessage(
            pinned = true,
            pinExpires = Date(currentTime + 1000),
        )

        advanceTimeBy(2000)

        message.isPinned { currentTime } shouldBeEqualTo false
    }

    @Test
    fun `validate message_isPinned when message gets deleted`() = runTest {
        val message = randomMessage(
            pinned = true,
            deletedAt = Date(currentTime),
        )

        advanceTimeBy(2000)

        message.isPinned { currentTime } shouldBeEqualTo false
    }

    @Test
    fun `validate message_isPinned when message gets unpinned`() = runTest {
        val message = randomMessage(
            pinned = false,
        )

        message.isPinned { currentTime } shouldBeEqualTo false
    }
}
