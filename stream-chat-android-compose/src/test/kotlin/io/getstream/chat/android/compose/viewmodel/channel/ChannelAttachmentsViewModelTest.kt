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
import io.getstream.chat.android.randomAttachmentType
import io.getstream.chat.android.randomCID
import io.getstream.chat.android.randomGenericError
import io.getstream.chat.android.ui.common.feature.channel.attachments.ChannelAttachmentsViewAction
import io.getstream.chat.android.ui.common.feature.channel.attachments.ChannelAttachmentsViewController
import io.getstream.chat.android.ui.common.feature.channel.attachments.ChannelAttachmentsViewEvent
import io.getstream.chat.android.ui.common.state.channel.attachments.ChannelAttachmentsViewState
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

internal class ChannelAttachmentsViewModelTest {

    @Test
    fun `state should reflect controller state`() = runTest {
        val stateFlow = MutableStateFlow<ChannelAttachmentsViewState>(ChannelAttachmentsViewState.Loading)
        val sut = Fixture()
            .givenControllerStateFlow(stateFlow)
            .get()

        sut.state.test {
            assertEquals(ChannelAttachmentsViewState.Loading, awaitItem())
        }
    }

    @Test
    fun `events should reflect controller events`() = runTest {
        val eventsFlow = MutableSharedFlow<ChannelAttachmentsViewEvent>()
        val sut = Fixture()
            .givenControllerEventsFlow(eventsFlow)
            .get()

        sut.events.test {
            val event = ChannelAttachmentsViewEvent.LoadMoreError(error = randomGenericError())
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

        val action = ChannelAttachmentsViewAction.LoadMoreRequested
        sut.onViewAction(action)

        fixture.verifyControllerOnViewAction(action)
    }

    private class Fixture {
        private val mockController: ChannelAttachmentsViewController = mock()

        fun givenControllerStateFlow(stateFlow: MutableStateFlow<ChannelAttachmentsViewState>) = apply {
            whenever(mockController.state) doReturn stateFlow
        }

        fun givenControllerEventsFlow(eventsFlow: MutableSharedFlow<ChannelAttachmentsViewEvent>) = apply {
            whenever(mockController.events) doReturn eventsFlow
        }

        fun verifyControllerOnViewAction(action: ChannelAttachmentsViewAction) = apply {
            verify(mockController).onViewAction(action)
        }

        fun get() = ChannelAttachmentsViewModel(
            cid = randomCID(),
            attachmentTypes = listOf(randomAttachmentType()),
            controllerProvider = { mockController },
        )
    }
}
