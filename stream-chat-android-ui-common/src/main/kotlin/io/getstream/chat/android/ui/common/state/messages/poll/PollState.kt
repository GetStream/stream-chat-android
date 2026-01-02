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

package io.getstream.chat.android.ui.common.state.messages.poll

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import io.getstream.chat.android.models.Message
import io.getstream.chat.android.models.Poll

/**
 * Holds the state of the poll.
 *
 * @property selectedPoll The more options that should be displayed.
 */
@Immutable
public data class PollState(
    public val selectedPoll: SelectedPoll? = null,
)

@Immutable
public data class SelectedPoll(
    val poll: Poll,
    val message: Message,
    val pollSelectionType: PollSelectionType,
)

@Stable
public sealed class PollSelectionType {
    public data object MoreOption : PollSelectionType()
    public data object ViewResult : PollSelectionType()
    public data object ViewAnswers : PollSelectionType()
}

internal fun PollState.stringify(): String {
    return "PollState(" +
        "selectedPoll: $selectedPoll)"
}
