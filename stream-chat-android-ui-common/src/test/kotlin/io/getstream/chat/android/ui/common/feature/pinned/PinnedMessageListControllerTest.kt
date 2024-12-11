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

package io.getstream.chat.android.ui.common.feature.pinned

import io.getstream.chat.android.client.channel.ChannelClient
import io.getstream.chat.android.models.Message
import io.getstream.chat.android.test.TestCall
import io.getstream.chat.android.test.TestCoroutineRule
import io.getstream.chat.android.test.callFrom
import io.getstream.chat.android.ui.common.model.MessageResult
import io.getstream.result.Error
import io.getstream.result.Result
import io.getstream.result.call.Call
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.amshove.kluent.`should be equal to`
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

internal class PinnedMessageListControllerTest {

    @get:Rule
    val testCoroutineRule = TestCoroutineRule()

    private val cid = "messaging:123"

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `Given loading new messages When the call fails Then state is updated and error is emitted`() = runTest {
        // given
        val returnValue = TestCall<List<Message>>(Result.Failure(Error.GenericError("Error")))
        val channelClient = mockChannelClient(returnValue)
        val controller = PinnedMessageListController(cid, channelClient)
        // when
        // observe errorEmissions
        val errorEmissions = mutableListOf<Unit>()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            controller.errorEvents.toList(errorEmissions)
        }
        val loadingState = controller.state.value
        controller.load()
        val errorState = controller.state.value
        // then
        // verify loading state
        loadingState.canLoadMore `should be equal to` true
        loadingState.isLoading `should be equal to` true
        loadingState.results `should be equal to` emptyList()
        // verify error state
        errorState.canLoadMore `should be equal to` true
        errorState.isLoading `should be equal to` false
        errorState.results `should be equal to` emptyList()
        // verify error emission
        errorEmissions.size `should be equal to` 1
    }

    @Test
    fun `Given loading new messages When the call returns all messages Then cannot load more`() = runTest {
        // given
        val pinnedMessages = generatePinnedMessages(count = 1)
        val returnValue = callFrom { pinnedMessages }
        val expectedResult = pinnedMessages.map { MessageResult(it, null) }
        val controller = PinnedMessageListController(cid, mockChannelClient(returnValue))
        // when
        val loadingState = controller.state.value
        controller.load()
        val loadedState = controller.state.value
        // then
        // verify loading state
        loadingState.canLoadMore `should be equal to` true
        loadingState.isLoading `should be equal to` true
        loadingState.results `should be equal to` emptyList()
        // verify loaded state
        loadedState.canLoadMore `should be equal to` false
        loadedState.isLoading `should be equal to` false
        loadedState.results `should be equal to` expectedResult
    }

    @Test
    fun `Given loading new messages When the call returns not all messages Then can load more`() = runTest {
        // given
        val pinnedMessages = generatePinnedMessages(count = 30)
        val returnValue = callFrom { pinnedMessages }
        val expectedResult = pinnedMessages.map { MessageResult(it, null) }
        val controller = PinnedMessageListController(cid, mockChannelClient(returnValue))
        // when
        val loadingState = controller.state.value
        controller.load()
        val loadedState = controller.state.value
        // then
        // verify loading state
        loadingState.canLoadMore `should be equal to` true
        loadingState.isLoading `should be equal to` true
        loadingState.results `should be equal to` emptyList()
        // verify loaded state
        loadedState.canLoadMore `should be equal to` true
        loadedState.isLoading `should be equal to` false
        loadedState.results `should be equal to` expectedResult
    }

    @Test
    fun `Given loading more messages When all messages are loaded Then call is ignored`() = runTest {
        // given
        val pinnedMessages = generatePinnedMessages(count = 1)
        val returnValue = callFrom { pinnedMessages }
        val expectedResult = pinnedMessages.map { MessageResult(it, null) }
        val channelClient = mockChannelClient(returnValue)
        val controller = PinnedMessageListController(cid, channelClient)
        // when
        controller.load() // initial load
        controller.loadMore() // load more
        val loadedState = controller.state.value
        // then
        // verify loaded state
        loadedState.canLoadMore `should be equal to` false
        loadedState.isLoading `should be equal to` false
        loadedState.results `should be equal to` expectedResult
        // verify channelClient.getPinnedMessages was called only twice
        verify(channelClient, times(1)).getPinnedMessages(any(), any(), any())
    }

    @Test
    fun `Given loading more messages When not all messages are loaded Then call is performed`() = runTest {
        // given
        val pinnedMessages = generatePinnedMessages(count = 30)
        val returnValue = callFrom { pinnedMessages }
        val channelClient = mockChannelClient(returnValue)
        val controller = PinnedMessageListController(cid, channelClient)
        // when
        controller.load() // initial load
        controller.loadMore() // load more
        val loadedState = controller.state.value
        // then
        // verify loaded state
        loadedState.canLoadMore `should be equal to` true
        loadedState.isLoading `should be equal to` false
        loadedState.results.size `should be equal to` pinnedMessages.size * 2
        // verify channelClient.getPinnedMessages was called twice
        verify(channelClient, times(2)).getPinnedMessages(any(), any(), any())
    }

    /**
     * Utilizes the fact that the initial state has isLoading = true
     */
    @Test
    fun `Given loading more messages When messages are already loading Then call is ignored`() = runTest {
        // given
        val pinnedMessages = generatePinnedMessages(count = 30)
        val returnValue = callFrom { pinnedMessages }
        val channelClient = mockChannelClient(returnValue)
        val controller = PinnedMessageListController(cid, channelClient)
        // when
        controller.loadMore()
        val state = controller.state.value
        // then
        state.canLoadMore `should be equal to` true
        state.isLoading `should be equal to` true
        state.results `should be equal to` emptyList()
        // verify channelClient.getPinnedMessages wasn't called
        verify(channelClient, times(0)).getPinnedMessages(any(), any(), any())
    }

    private fun mockChannelClient(
        returnValue: Call<List<Message>>,
    ): ChannelClient {
        val channelClient = mock<ChannelClient>()
        whenever(channelClient.getPinnedMessages(any(), any(), any())) doReturn returnValue
        return channelClient
    }

    private fun generatePinnedMessages(count: Int): List<Message> = List(count) { index ->
        Message(id = "$index", text = "Pinned message $index")
    }
}
