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

package io.getstream.chat.android.compose.ui.util

import android.content.Context
import androidx.compose.ui.text.input.KeyboardType
import io.getstream.chat.android.compose.R
import io.getstream.chat.android.compose.ui.messages.attachments.poll.PollSwitchItemKeys
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

internal class DefaultPollSwitchItemFactoryTest {

    @Test
    fun testDefaultPollSwitchItemFactory() {
        // given
        val context = mock<Context>()
        whenever(context.getString(R.string.stream_compose_poll_option_switch_multiple_answers))
            .thenReturn("Multiple answers")
        whenever(context.getString(R.string.stream_compose_poll_option_max_number_of_answers_hint))
            .thenReturn("Max number of answers")
        whenever(context.getString(R.string.stream_compose_poll_option_switch_anonymous_poll))
            .thenReturn("Anonymous poll")
        whenever(context.getString(R.string.stream_compose_poll_option_switch_suggest_option))
            .thenReturn("Suggest an option")
        whenever(context.getString(R.string.stream_compose_poll_option_switch_add_comment))
            .thenReturn("Add a comment")
        // when
        val items = PollSwitchItemFactory
            .defaultFactory(context)
            .providePollSwitchItemList()
        // then
        // Size
        assertEquals(4, items.size)
        // Max votes item
        assertEquals("Multiple answers", items[0].title)
        assertEquals(PollSwitchItemKeys.MAX_VOTES_ALLOWED, items[0].key)
        assertFalse(items[0].enabled)
        assertEquals("", items[0].pollSwitchInput?.value)
        assertEquals(2, items[0].pollSwitchInput?.minValue)
        assertEquals(10, items[0].pollSwitchInput?.maxValue)
        assertEquals("Max number of answers", items[0].pollSwitchInput?.description)
        assertEquals(KeyboardType.Number, items[0].pollSwitchInput?.keyboardType)
        assertNull(items[0].pollOptionError)
        // Anonymous poll item
        assertEquals("Anonymous poll", items[1].title)
        assertEquals(PollSwitchItemKeys.VOTING_VISIBILITY, items[1].key)
        assertFalse(items[1].enabled)
        assertNull(items[1].pollSwitchInput)
        assertNull(items[1].pollOptionError)
        // Hide results item
        assertEquals("Suggest an option", items[2].title)
        assertEquals(PollSwitchItemKeys.ALLOW_USER_SUGGESTED_OPTIONS, items[2].key)
        assertFalse(items[2].enabled)
        assertNull(items[2].pollSwitchInput)
        assertNull(items[2].pollOptionError)
        // Close poll item
        assertEquals("Add a comment", items[3].title)
        assertEquals(PollSwitchItemKeys.ALLOW_ANSWERS, items[3].key)
        assertFalse(items[3].enabled)
        assertNull(items[3].pollSwitchInput)
        assertNull(items[3].pollOptionError)
    }
}
