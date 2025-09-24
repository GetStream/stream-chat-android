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

package io.getstream.chat.android.compose.viewmodel.channel

import app.cash.turbine.test
import io.getstream.chat.android.randomCID
import io.getstream.chat.android.randomMember
import io.getstream.chat.android.randomUser
import io.getstream.chat.android.ui.common.feature.channel.info.ChannelInfoMemberViewEvent
import io.getstream.chat.android.ui.common.feature.channel.info.ChannelInfoViewAction
import io.getstream.chat.android.ui.common.feature.channel.info.ChannelInfoViewController
import io.getstream.chat.android.ui.common.feature.channel.info.ChannelInfoViewEvent
import io.getstream.chat.android.ui.common.state.channel.info.ChannelInfoViewState
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

internal class ChannelInfoViewModelTest {

    @Test
    fun `state should reflect controller state`() = runTest {
        val stateFlow = MutableStateFlow<ChannelInfoViewState>(ChannelInfoViewState.Loading)
        val sut = Fixture()
            .givenControllerStateFlow(stateFlow)
            .get()

        sut.state.test {
            assertEquals(ChannelInfoViewState.Loading, awaitItem())
        }
    }

    @Test
    fun `events should reflect controller events`() = runTest {
        val eventsFlow = MutableSharedFlow<ChannelInfoViewEvent>()
        val sut = Fixture()
            .givenControllerEventsFlow(eventsFlow)
            .get()

        sut.events.test {
            val event = ChannelInfoViewEvent.NavigateToChannel(cid = randomCID())
            eventsFlow.emit(event)

            assertEquals(
                event,
                awaitItem(),
            )
        }
    }

    @Test
    fun `onViewAction should delegate to controller`() = runTest {
        val fixture = Fixture()
        val sut = fixture.get()

        val action = ChannelInfoViewAction.UserInfoClick(user = randomUser())
        sut.onViewAction(action)

        fixture.verifyControllerOnViewAction(action)
    }

    @Test
    fun `onMemberViewEvent should delegate to controller`() = runTest {
        val fixture = Fixture()
        val sut = fixture.get()

        val event = ChannelInfoMemberViewEvent.BanMember(member = randomMember())
        sut.onMemberViewEvent(event)

        fixture.verifyControllerOnMemberViewEvent(event)
    }

    private class Fixture {
        private val mockController: ChannelInfoViewController = mock()

        fun givenControllerStateFlow(stateFlow: MutableStateFlow<ChannelInfoViewState>) = apply {
            whenever(mockController.state) doReturn stateFlow
        }

        fun givenControllerEventsFlow(eventsFlow: MutableSharedFlow<ChannelInfoViewEvent>) = apply {
            whenever(mockController.events) doReturn eventsFlow
        }

        fun verifyControllerOnViewAction(action: ChannelInfoViewAction) = apply {
            verify(mockController).onViewAction(action)
        }

        fun verifyControllerOnMemberViewEvent(event: ChannelInfoMemberViewEvent) = apply {
            verify(mockController).onMemberViewEvent(event)
        }

        fun get() = ChannelInfoViewModel(
            cid = randomCID(),
            copyToClipboardHandler = mock(),
            optionFilter = mock(),
            controllerProvider = { mockController },
        )
    }
}
