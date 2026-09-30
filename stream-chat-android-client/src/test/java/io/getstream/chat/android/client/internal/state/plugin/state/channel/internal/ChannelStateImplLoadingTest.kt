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

package io.getstream.chat.android.client.internal.state.plugin.state.channel.internal

import io.getstream.chat.android.client.api.models.Pagination
import io.getstream.chat.android.client.api.models.QueryChannelRequest
import io.getstream.chat.android.models.ChannelData
import io.getstream.chat.android.models.MessagesState
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

@OptIn(ExperimentalCoroutinesApi::class)
internal class ChannelStateImplLoadingTest : ChannelStateImplTestBase() {

    @Test
    fun `setLoadingIfEmpty on a channel without data should show the loading state`() = runTest {
        channelState.setLoadingIfEmpty()

        assertTrue(channelState.loading.value)
        assertEquals(MessagesState.Loading, channelState.messagesState.value)
    }

    @Test
    fun `setLoadingIfEmpty on a channel with data should not show the loading state`() = runTest {
        channelState.updateChannelData { ChannelData(type = CHANNEL_TYPE, id = CHANNEL_ID) }

        channelState.setLoadingIfEmpty()

        assertFalse(channelState.loading.value)
        assertEquals(MessagesState.OfflineNoResults, channelState.messagesState.value)
    }

    @Test
    fun `endFirstPageLoad should end the loading state`() = runTest {
        channelState.setLoadingIfEmpty()

        channelState.endFirstPageLoad()

        assertFalse(channelState.loading.value)
    }

    @Test
    fun `loading should still follow message pagination`() = runTest {
        channelState.paginationManager.begin(QueryChannelRequest().withMessages(Pagination.LESS_THAN, "message_1", 30))

        assertTrue(channelState.loading.value)
    }

    @Test
    fun `messagesState should not report no results while the loaded messages are still propagating`() =
        runTest(StandardTestDispatcher(testCoroutines.dispatcher.scheduler)) {
            val emissions = mutableListOf<MessagesState>()
            backgroundScope.launch { channelState.messagesState.collect { emissions += it } }
            channelState.setLoadingIfEmpty()
            runCurrent()

            channelState.setMessages(createMessages(3))
            channelState.endFirstPageLoad()
            runCurrent()

            val afterLoading = emissions.dropWhile { it != MessagesState.Loading }
            assertFalse(afterLoading.contains(MessagesState.OfflineNoResults))
            assertTrue(afterLoading.last() is MessagesState.Result)
        }
}
