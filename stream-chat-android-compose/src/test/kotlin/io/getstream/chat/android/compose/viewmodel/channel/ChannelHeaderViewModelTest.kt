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
import io.getstream.chat.android.ui.common.feature.channel.header.ChannelHeaderViewController
import io.getstream.chat.android.ui.common.state.messages.list.ChannelHeaderViewState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

internal class ChannelHeaderViewModelTest {

    @Test
    fun `state should reflect controller state`() = runTest {
        val stateFlow = MutableStateFlow<ChannelHeaderViewState>(ChannelHeaderViewState.Loading)
        val sut = Fixture()
            .givenControllerStateFlow(stateFlow)
            .get()

        sut.state.test {
            assertEquals(ChannelHeaderViewState.Loading, awaitItem())
        }
    }

    private class Fixture {
        private val mockController: ChannelHeaderViewController = mock()

        fun givenControllerStateFlow(stateFlow: MutableStateFlow<ChannelHeaderViewState>) = apply {
            whenever(mockController.state) doReturn stateFlow
        }

        fun get() = ChannelHeaderViewModel(
            cid = randomCID(),
            controllerProvider = { mockController },
        )
    }
}
