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

package io.getstream.chat.android.ui.viewmodel.messages

import io.getstream.chat.android.client.api.state.ThreadState
import io.getstream.chat.android.randomMessage
import io.getstream.chat.android.ui.common.state.messages.MessageMode
import io.getstream.chat.android.ui.common.state.messages.list.MessageListState
import org.amshove.kluent.shouldBeEqualTo
import org.junit.jupiter.api.Test
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock

internal class ShouldRequestMessagesAtBottomTest {

    @Test
    fun `Given a channel When inside a search Should request messages at the bottom`() {
        shouldRequest(isInsideSearch = true, mode = MessageMode.Normal) shouldBeEqualTo true
        shouldRequest(isInsideSearch = false, mode = MessageMode.Normal) shouldBeEqualTo false
    }

    @Test
    fun `Given a thread without a jump page Should request messages at the bottom only in the older-to-newer order`() {
        val mode = threadMode(isJumpPage = false)

        shouldRequest(mode = mode, olderToNewer = false) shouldBeEqualTo false
        shouldRequest(mode = mode, olderToNewer = true) shouldBeEqualTo true
    }

    @Test
    fun `Given a jump page without the newest replies Should request messages at the bottom`() {
        shouldRequest(mode = threadMode(isJumpPage = true), endOfNewMessages = false) shouldBeEqualTo true
    }

    @Test
    fun `Given a jump page that reaches the newest replies Should not request messages at the bottom`() {
        shouldRequest(mode = threadMode(isJumpPage = true), endOfNewMessages = true) shouldBeEqualTo false
    }

    private fun shouldRequest(
        mode: MessageMode,
        isInsideSearch: Boolean = false,
        endOfNewMessages: Boolean = false,
        olderToNewer: Boolean = false,
    ): Boolean = shouldRequestMessagesAtBottom(
        isInsideSearch = isInsideSearch,
        mode = mode,
        threadListState = MessageListState(endOfNewMessagesReached = endOfNewMessages),
        threadLoadOrderOlderToNewer = olderToNewer,
    )

    private fun threadMode(isJumpPage: Boolean): MessageMode.MessageThread {
        val threadState: ThreadState = mock { on { it.isJumpPage } doReturn isJumpPage }
        return MessageMode.MessageThread(randomMessage(), threadState)
    }
}
