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

package io.getstream.chat.android.compose.viewmodel.mentions

import app.cash.turbine.test
import io.getstream.chat.android.randomString
import io.getstream.chat.android.ui.common.feature.mentions.MentionListController
import io.getstream.chat.android.ui.common.state.mentions.MentionListEvent
import io.getstream.chat.android.ui.common.state.mentions.MentionListState
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

internal class MentionListViewModelTest {

    @Test
    fun `state should reflect controller state`() = runTest {
        val stateFlow = MutableStateFlow(MentionListState())
        val sut = Fixture()
            .givenControllerStateFlow(stateFlow)
            .get()

        sut.state.test {
            assertEquals(MentionListState(), awaitItem())
        }
    }

    @Test
    fun `events should reflect controller events`() = runTest {
        val eventsFlow = MutableSharedFlow<MentionListEvent>()
        val sut = Fixture()
            .givenControllerEventsFlow(eventsFlow)
            .get()

        sut.events.test {
            val event = MentionListEvent.Error(message = randomString())
            eventsFlow.emit(event)

            assertEquals(
                event,
                awaitItem(),
            )
        }
    }

    @Test
    fun `loadMore should delegate to controller`() = runTest {
        val fixture = Fixture()
        val sut = fixture.get()

        sut.loadMore()

        fixture.verifyLoadMore()
    }

    @Test
    fun `refresh should delegate to controller`() = runTest {
        val fixture = Fixture()
        val sut = fixture.get()

        sut.refresh()

        fixture.verifyRefresh()
    }

    private class Fixture {
        private val mockController: MentionListController = mock()

        fun givenControllerStateFlow(stateFlow: MutableStateFlow<MentionListState>) = apply {
            whenever(mockController.state) doReturn stateFlow
        }

        fun givenControllerEventsFlow(eventsFlow: MutableSharedFlow<MentionListEvent>) = apply {
            whenever(mockController.events) doReturn eventsFlow
        }

        fun verifyLoadMore() = apply {
            verify(mockController).loadMore()
        }

        fun verifyRefresh() = apply {
            verify(mockController).refresh()
        }

        fun get() = MentionListViewModel(
            controllerProvider = { mockController },
        )
    }
}
