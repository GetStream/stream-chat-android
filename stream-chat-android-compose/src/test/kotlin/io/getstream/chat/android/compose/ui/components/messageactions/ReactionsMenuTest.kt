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

package io.getstream.chat.android.compose.ui.components.messageactions

import app.cash.paparazzi.DeviceConfig
import app.cash.paparazzi.Paparazzi
import io.getstream.chat.android.compose.ui.PaparazziComposeTest
import io.getstream.chat.android.models.QueryReactionsResult
import io.getstream.chat.android.models.Reaction
import io.getstream.chat.android.previewdata.PreviewReactionData
import io.getstream.chat.android.test.asCall
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.anyOrNull
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.whenever

internal class ReactionsMenuTest : PaparazziComposeTest {

    @get:Rule
    override val paparazzi = Paparazzi(deviceConfig = DeviceConfig.PIXEL_2)

    @Test
    fun `reactions menu one reaction`() {
        stubReactions(PreviewReactionData.oneReaction)
        snapshot { ReactionsMenuSampleOneReaction() }
    }

    @Test
    fun `reactions menu one reaction in dark mode`() {
        stubReactions(PreviewReactionData.oneReaction)
        snapshot(isInDarkMode = true) { ReactionsMenuSampleOneReaction() }
    }

    @Test
    fun `reactions menu many reactions`() {
        stubReactions(PreviewReactionData.manyReaction)
        snapshot { ReactionsMenuSampleManyReactions() }
    }

    @Test
    fun `reactions menu many reactions in dark mode`() {
        stubReactions(PreviewReactionData.manyReaction)
        snapshot(isInDarkMode = true) { ReactionsMenuSampleManyReactions() }
    }

    /**
     * Stubs `chatClient.queryReactions(...)` to echo the input [reactions] back as a successful
     * paginated response, so [io.getstream.chat.android.compose.viewmodel.messages.ReactionsMenuViewModel]'s
     * `init` coroutine completes without overwriting state and without leaking an uncaught
     * exception into the JVM-wide handler.
     *
     * Without this stub, the mocked Call's `await()` returns null (Mockito default for unstubbed
     * suspend methods), the `.onSuccess { ... }` block NPEs inside the ViewModel's launch, and the
     * uncaught exception breaks downstream `runTest` scopes that share the same JVM (e.g.
     * AddMembersViewModelTest).
     */
    private fun stubReactions(reactions: List<Reaction>) {
        val result = QueryReactionsResult(reactions = reactions, next = null)
        whenever(
            mockChatClient.queryReactions(
                messageId = any(),
                filter = anyOrNull(),
                limit = anyOrNull(),
                next = anyOrNull(),
                sort = anyOrNull(),
            ),
        ) doReturn result.asCall()
    }
}
