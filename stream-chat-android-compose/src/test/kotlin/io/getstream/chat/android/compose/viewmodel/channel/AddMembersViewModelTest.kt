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

package io.getstream.chat.android.compose.viewmodel.channel

import app.cash.turbine.test
import io.getstream.chat.android.randomCID
import io.getstream.chat.android.randomUser
import io.getstream.chat.android.ui.common.feature.channel.info.AddMembersViewAction
import io.getstream.chat.android.ui.common.feature.channel.info.AddMembersViewController
import io.getstream.chat.android.ui.common.state.channel.info.AddMembersViewState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

internal class AddMembersViewModelTest {

    @Test
    fun `state should reflect controller state`() = runTest {
        val stateFlow = MutableStateFlow(AddMembersViewState())
        val sut = Fixture()
            .givenControllerStateFlow(stateFlow)
            .get()

        sut.state.test {
            assertEquals(AddMembersViewState(), awaitItem())
        }
    }

    @Test
    fun `state updates are propagated from controller`() = runTest {
        val stateFlow = MutableStateFlow(AddMembersViewState())
        val sut = Fixture()
            .givenControllerStateFlow(stateFlow)
            .get()

        sut.state.test {
            skipItems(1) // Skip initial state

            val updatedState = AddMembersViewState(isLoading = false, query = "Alice")
            stateFlow.value = updatedState

            assertEquals(updatedState, awaitItem())
        }
    }

    @Test
    fun `onViewAction should delegate to controller`() = runTest {
        val fixture = Fixture()
        val sut = fixture.get()

        val action = AddMembersViewAction.UserClick(user = randomUser())
        sut.onViewAction(action)

        fixture.verifyControllerOnViewAction(action)
    }

    @Test
    fun `onViewAction QueryChanged should delegate to controller`() = runTest {
        val fixture = Fixture()
        val sut = fixture.get()

        val action = AddMembersViewAction.QueryChanged(query = "search query")
        sut.onViewAction(action)

        fixture.verifyControllerOnViewAction(action)
    }

    @Test
    fun `onViewAction LoadMore should delegate to controller`() = runTest {
        val fixture = Fixture()
        val sut = fixture.get()

        sut.onViewAction(AddMembersViewAction.LoadMore)

        fixture.verifyControllerOnViewAction(AddMembersViewAction.LoadMore)
    }

    private class Fixture {
        private val mockController: AddMembersViewController = mock()

        fun givenControllerStateFlow(stateFlow: MutableStateFlow<AddMembersViewState>) = apply {
            whenever(mockController.state) doReturn stateFlow
        }

        fun verifyControllerOnViewAction(action: AddMembersViewAction) = apply {
            verify(mockController).onViewAction(action)
        }

        fun get() = AddMembersViewModel(
            cid = randomCID(),
            controllerProvider = { mockController },
        )
    }
}
