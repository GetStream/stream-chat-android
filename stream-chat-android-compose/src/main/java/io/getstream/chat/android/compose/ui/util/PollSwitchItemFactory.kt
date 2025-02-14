/*
 * Copyright (c) 2014-2024 Stream.io Inc. All rights reserved.
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
import androidx.compose.runtime.Stable
import androidx.compose.ui.text.input.KeyboardType
import io.getstream.chat.android.compose.R
import io.getstream.chat.android.compose.ui.messages.attachments.poll.PollSwitchInput
import io.getstream.chat.android.compose.ui.messages.attachments.poll.PollSwitchItem
import io.getstream.chat.android.compose.ui.messages.attachments.poll.PollSwitchItemKeys

/**
 * An interface that allows the creation of poll switch items for the creation screen.
 */
@Stable
public interface PollSwitchItemFactory {

    /**
     * Provides a list of [PollSwitchItem] to create the poll switch item list.
     */
    public fun providePollSwitchItemList(): List<PollSwitchItem>

    public companion object {

        /**
         * Builds the default poll switch item factory that holds [PollSwitchItem].
         *
         * @return The default implementation opf [PollSwitchItemFactory].
         */
        public fun defaultFactory(context: Context): PollSwitchItemFactory =
            DefaultPollSwitchItemFactory(context = context)
    }
}

/**
 * The default implementation of [PollSwitchItemFactory] that holds the default poll switch items.ø
 */
public class DefaultPollSwitchItemFactory(
    private val context: Context,
) : PollSwitchItemFactory {

    /**
     * Provides a default list of [PollSwitchItem] to create the poll switch item list.
     */
    override fun providePollSwitchItemList(): List<PollSwitchItem> =
        listOf(
            PollSwitchItem(
                title = context.getString(R.string.stream_compose_poll_option_switch_multiple_answers),
                pollSwitchInput = PollSwitchInput(
                    value = "",
                    description = context.getString(R.string.stream_compose_poll_option_max_number_of_answers_hint),
                    minValue = 1,
                    maxValue = 2,
                    keyboardType = KeyboardType.Decimal,
                ),
                key = PollSwitchItemKeys.MAX_VOTES_ALLOWED,
                enabled = false,
            ),
            PollSwitchItem(
                title = context.getString(R.string.stream_compose_poll_option_switch_anonymous_poll),
                key = PollSwitchItemKeys.VOTING_VISIBILITY,
                enabled = false,
            ),
            PollSwitchItem(
                title = context.getString(R.string.stream_compose_poll_option_switch_suggest_option),
                key = PollSwitchItemKeys.ALLOW_USER_SUGGESTED_OPTIONS,
                enabled = false,
            ),
            PollSwitchItem(
                title = context.getString(R.string.stream_compose_poll_option_switch_add_comment),
                key = PollSwitchItemKeys.ALLOW_ANSWERS,
                enabled = false,
            ),
        )
}
