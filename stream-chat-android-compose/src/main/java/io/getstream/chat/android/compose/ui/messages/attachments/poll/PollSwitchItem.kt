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

package io.getstream.chat.android.compose.ui.messages.attachments.poll

/**
 * Represents a single feature toggle in the poll creation screen.
 *
 * Each subtype carries only the data it needs. [PollSwitchList] pattern-matches on the concrete
 * type to decide how to render each item.
 */
internal sealed interface PollSwitchItem {
    val enabled: Boolean
    val onCheckedChange: (Boolean) -> Unit

    /**
     * "Multiple votes" toggle with an input for the max votes per person.
     */
    data class MultipleVotes(
        override val enabled: Boolean,
        override val onCheckedChange: (Boolean) -> Unit,
        val maxVotesPerUser: Int,
        val onMaxVotesCommit: (String) -> Unit,
    ) : PollSwitchItem

    /**
     * "Anonymous poll" toggle.
     */
    data class AnonymousPoll(
        override val enabled: Boolean,
        override val onCheckedChange: (Boolean) -> Unit,
    ) : PollSwitchItem

    /**
     * "Suggest an option" toggle.
     */
    data class SuggestAnOption(
        override val enabled: Boolean,
        override val onCheckedChange: (Boolean) -> Unit,
    ) : PollSwitchItem

    /**
     * "Allow comments" toggle.
     */
    data class AllowComments(
        override val enabled: Boolean,
        override val onCheckedChange: (Boolean) -> Unit,
    ) : PollSwitchItem
}
