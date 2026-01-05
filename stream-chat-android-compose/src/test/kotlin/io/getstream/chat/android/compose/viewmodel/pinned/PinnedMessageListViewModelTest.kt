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

package io.getstream.chat.android.compose.viewmodel.pinned

import app.cash.turbine.test
import io.getstream.chat.android.randomBoolean
import io.getstream.chat.android.randomChannel
import io.getstream.chat.android.randomDate
import io.getstream.chat.android.randomMessage
import io.getstream.chat.android.ui.common.feature.pinned.PinnedMessageListController
import io.getstream.chat.android.ui.common.model.MessageResult
import io.getstream.chat.android.ui.common.state.pinned.PinnedMessageListState
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

internal class PinnedMessageListViewModelTest {

    @Test
    fun `state should reflect controller state`() = runTest {
        val state = PinnedMessageListState(
            canLoadMore = randomBoolean(),
            results = listOf(MessageResult(message = randomMessage(), channel = randomChannel())),
            isLoading = randomBoolean(),
            nextDate = randomDate(),
        )
        val stateFlow = MutableStateFlow(state)
        val sut = Fixture()
            .givenControllerStateFlow(stateFlow)
            .get()

        sut.state.test {
            assertEquals(state, awaitItem())
        }
    }

    @Test
    fun `errorEvents should reflect controller events`() = runTest {
        val eventsFlow = MutableSharedFlow<Unit>()
        val sut = Fixture()
            .givenControllerErrorEventsFlow(eventsFlow)
            .get()

        sut.errorEvents.test {
            eventsFlow.emit(Unit)

            assertEquals(
                Unit,
                awaitItem(),
            )
        }
    }

    @Test
    fun `init should call load on controller`() = runTest {
        val fixture = Fixture()
        fixture.get()

        fixture.verifyLoad()
    }

    @Test
    fun `loadMore should delegate to controller`() = runTest {
        val fixture = Fixture()
        val sut = fixture.get()

        sut.loadMore()

        fixture.verifyLoadMore()
    }

    private class Fixture {
        private val mockController: PinnedMessageListController = mock()

        fun givenControllerStateFlow(stateFlow: MutableStateFlow<PinnedMessageListState>) = apply {
            whenever(mockController.state) doReturn stateFlow
        }

        fun givenControllerErrorEventsFlow(errorEventsFlow: MutableSharedFlow<Unit>) = apply {
            whenever(mockController.errorEvents) doReturn errorEventsFlow
        }

        fun verifyLoad() = apply {
            verify(mockController).load()
        }

        fun verifyLoadMore() = apply {
            verify(mockController).loadMore()
        }

        fun get() = PinnedMessageListViewModel(
            controller = mockController,
        )
    }
}
