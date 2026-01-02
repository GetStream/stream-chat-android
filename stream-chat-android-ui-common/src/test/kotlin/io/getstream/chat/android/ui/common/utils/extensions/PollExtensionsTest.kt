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

package io.getstream.chat.android.ui.common.utils.extensions

import android.content.Context
import io.getstream.chat.android.randomOption
import io.getstream.chat.android.randomPoll
import io.getstream.chat.android.ui.common.R
import org.junit.Assert.assertEquals
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

internal class PollExtensionsTest {

    private val context: Context = mock()

    @Test
    fun `getSubtitle should return single answer subtitle when maxVotesAllowed is 1`() {
        val poll = randomPoll(maxVotesAllowed = 1, closed = false)
        whenever(context.getString(R.string.stream_ui_poll_description_single_answer))
            .thenReturn("You can select only one answer.")

        val result = poll.getSubtitle(context)

        assertEquals("You can select only one answer.", result)
    }

    @Test
    fun `getSubtitle should return multiple answers subtitle when maxVotesAllowed is greater than 1`() {
        val poll = randomPoll(
            options = listOf(randomOption(), randomOption()),
            maxVotesAllowed = 2,
            closed = false,
        )
        whenever(context.getString(R.string.stream_ui_poll_description_multiple_answers, 2))
            .thenReturn("You can select up to 2 answers.")

        val result = poll.getSubtitle(context)

        assertEquals("You can select up to 2 answers.", result)
    }

    @Test
    fun `getSubtitle should respect the minimum of maxVotesAllowed and options size`() {
        val poll = randomPoll(maxVotesAllowed = 3, closed = false)
        whenever(context.getString(R.string.stream_ui_poll_description_single_answer))
            .thenReturn("You can select only one answer.")

        val result = poll.getSubtitle(context)

        assertEquals("You can select only one answer.", result)
    }

    @Test
    fun `getSubtitle should return closed subtitle when poll is closed`() {
        val poll = randomPoll(closed = true)
        whenever(context.getString(R.string.stream_ui_poll_description_closed))
            .thenReturn("This poll is closed.")

        val result = poll.getSubtitle(context)

        assertEquals("This poll is closed.", result)
    }
}
