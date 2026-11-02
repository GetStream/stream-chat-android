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

package io.getstream.chat.android.client.internal.state.plugin.logic.querychannels.internal

import io.getstream.chat.android.models.ScheduledMessage
import io.getstream.chat.android.randomScheduledMessage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.times
import org.mockito.kotlin.verify

internal class ScheduledMessagesChannelsRefresherTest {

    private val queryChannelsLogic: QueryChannelsLogic = mock()

    @Test
    fun `refreshes the channels whose scheduled messages change, in every active query`() = runTest {
        val first = randomScheduledMessage()
        val second = randomScheduledMessage()
        val scheduledMessages = MutableStateFlow(emptyList<ScheduledMessage>())
        backgroundScope.refreshChannelsOnScheduledMessages(scheduledMessages) { listOf(queryChannelsLogic) }
        runCurrent()
        verify(queryChannelsLogic, never()).refreshChannelsState(any())

        scheduledMessages.value = listOf(first)
        runCurrent()
        verify(queryChannelsLogic).refreshChannelsState(listOf(first.cid))

        scheduledMessages.value = listOf(first, second)
        runCurrent()
        verify(queryChannelsLogic).refreshChannelsState(listOf(second.cid))

        scheduledMessages.value = listOf(second)
        runCurrent()
        verify(queryChannelsLogic, times(2)).refreshChannelsState(listOf(first.cid))
    }
}
