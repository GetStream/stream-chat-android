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

package io.getstream.chat.android.compose.ui.util

import android.content.Context
import androidx.compose.ui.text.input.KeyboardType
import io.getstream.chat.android.compose.R
import org.junit.jupiter.api.Assertions
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
        Assertions.assertEquals(4, items.size)
        // Max votes item
        Assertions.assertEquals("Multiple answers", items[0].title)
        Assertions.assertEquals("maxVotesAllowed", items[0].key)
        Assertions.assertFalse(items[0].enabled)
        Assertions.assertEquals(0, items[0].pollSwitchInput?.value)
        Assertions.assertEquals(2, items[0].pollSwitchInput?.maxValue)
        Assertions.assertEquals("Max number of answers", items[0].pollSwitchInput?.description)
        Assertions.assertEquals(KeyboardType.Decimal, items[0].pollSwitchInput?.keyboardType)
        Assertions.assertNull(items[0].pollOptionError)
        // Anonymous poll item
        Assertions.assertEquals("Anonymous poll", items[1].title)
        Assertions.assertEquals("votingVisibility", items[1].key)
        Assertions.assertFalse(items[1].enabled)
        Assertions.assertNull(items[1].pollSwitchInput)
        Assertions.assertNull(items[1].pollOptionError)
        // Hide results item
        Assertions.assertEquals("Suggest an option", items[2].title)
        Assertions.assertEquals("allowUserSuggestedOptions", items[2].key)
        Assertions.assertFalse(items[2].enabled)
        Assertions.assertNull(items[2].pollSwitchInput)
        Assertions.assertNull(items[2].pollOptionError)
        // Close poll item
        Assertions.assertEquals("Add a comment", items[3].title)
        Assertions.assertEquals("allowAnswers", items[3].key)
        Assertions.assertFalse(items[3].enabled)
        Assertions.assertNull(items[3].pollSwitchInput)
        Assertions.assertNull(items[3].pollOptionError)
    }
}
