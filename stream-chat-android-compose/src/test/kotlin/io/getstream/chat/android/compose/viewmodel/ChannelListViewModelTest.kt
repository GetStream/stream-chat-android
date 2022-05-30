/*
 * Copyright (c) 2014-2022 Stream.io Inc. All rights reserved.
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

package io.getstream.chat.android.compose.viewmodel

import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.api.models.FilterObject
import io.getstream.chat.android.client.api.models.QueryChannelsRequest
import io.getstream.chat.android.client.api.models.QuerySort
import io.getstream.chat.android.client.models.Channel
import io.getstream.chat.android.client.models.Filters
import io.getstream.chat.android.client.models.User
import io.getstream.chat.android.compose.viewmodel.channels.ChannelListViewModel
import io.getstream.chat.android.offline.plugin.state.StateRegistry
import io.getstream.chat.android.offline.plugin.state.global.internal.GlobalMutableState
import io.getstream.chat.android.offline.plugin.state.querychannels.ChannelsStateData
import io.getstream.chat.android.offline.plugin.state.querychannels.QueryChannelsState
import io.getstream.chat.android.test.TestCoroutineExtension
import io.getstream.chat.android.test.asCall
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.amshove.kluent.internal.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

internal class ChannelListViewModelTest {

    companion object {
        @JvmField
        @RegisterExtension
        val testCoroutines = TestCoroutineExtension()
    }

    @Test
    fun sampleTest() = runTest {
        val viewModel = Fixture()
            .givenCurrentUser()
            .givenChannelsQuery()
            .givenChannelsState()
            .givenChannelMutes()
            .get()

        assertEquals(0, viewModel.channelsState.channelItems.size)
    }

    private class Fixture(
        private val chatClient: ChatClient = mock(),
        private val initialSort: QuerySort<Channel> = QuerySort.desc("last_updated"),
        private val initialFilters: FilterObject? = Filters.and(
            Filters.eq("type", "messaging"),
            Filters.`in`("members", "jc")
        ),
    ) {
        private val globalState: GlobalMutableState = mock()
        private val stateRegistry: StateRegistry = mock()

        init {
            StateRegistry.instance = stateRegistry
            GlobalMutableState.instance = globalState
        }

        fun givenCurrentUser(currentUser: User = User(id = "Jc")) = apply {
            whenever(globalState.user) doReturn MutableStateFlow(currentUser)
        }

        fun givenChannelMutes() = apply {
            whenever(globalState.channelMutes) doReturn MutableStateFlow(emptyList())
        }

        fun givenChannelsQuery(channels: List<Channel> = emptyList()) = apply {
            whenever(chatClient.queryChannels(any())) doReturn channels.asCall()
        }

        fun givenChannelsState() = apply {
            val queryChannelsState: QueryChannelsState = mock {
                whenever(it.channelsStateData) doReturn MutableStateFlow(ChannelsStateData.Loading)
                whenever(it.loadingMore) doReturn MutableStateFlow(false)
                whenever(it.endOfChannels) doReturn MutableStateFlow(false)
                whenever(it.channels) doReturn MutableStateFlow(null)
                whenever(it.nextPageRequest) doReturn MutableStateFlow<QueryChannelsRequest?>(null)
            }
            whenever(stateRegistry.queryChannels(any(), any())) doReturn queryChannelsState
        }

        fun get(): ChannelListViewModel {
            return ChannelListViewModel(
                chatClient = chatClient,
                initialSort = initialSort,
                initialFilters = initialFilters,
            )
        }
    }
}
