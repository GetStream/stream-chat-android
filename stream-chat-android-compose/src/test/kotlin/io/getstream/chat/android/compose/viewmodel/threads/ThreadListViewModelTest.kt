/*
 * Copyright (c) 2014-2025 Stream.io Inc. All rights reserved.
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

package io.getstream.chat.android.compose.viewmodel.threads

import app.cash.turbine.test
import io.getstream.chat.android.randomBoolean
import io.getstream.chat.android.randomInt
import io.getstream.chat.android.randomThread
import io.getstream.chat.android.ui.common.feature.threads.ThreadListController
import io.getstream.chat.android.ui.common.state.threads.ThreadListState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

internal class ThreadListViewModelTest {

    @Test
    fun `state should reflect controller state`() = runTest {
        val state = ThreadListState(
            threads = listOf(randomThread()),
            isLoading = randomBoolean(),
            isLoadingMore = randomBoolean(),
            unseenThreadsCount = randomInt(),
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
    fun `load should delegate to controller`() = runTest {
        val fixture = Fixture()
        val sut = fixture.get()

        sut.load()

        fixture.verifyLoad()
    }

    @Test
    fun `loadNextPage should delegate to controller`() = runTest {
        val fixture = Fixture()
        val sut = fixture.get()

        sut.loadNextPage()

        fixture.verifyLoadNextPage()
    }

    private class Fixture {
        private val mockController: ThreadListController = mock()

        fun givenControllerStateFlow(stateFlow: MutableStateFlow<ThreadListState>) = apply {
            whenever(mockController.state) doReturn stateFlow
        }

        fun verifyLoad() = apply {
            verify(mockController).load()
        }

        fun verifyLoadNextPage() = apply {
            verify(mockController).loadNextPage()
        }

        fun get() = ThreadListViewModel(
            controller = mockController,
        )
    }
}
