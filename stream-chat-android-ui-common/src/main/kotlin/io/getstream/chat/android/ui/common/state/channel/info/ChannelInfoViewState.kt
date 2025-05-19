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

package io.getstream.chat.android.ui.common.state.channel.info

import io.getstream.chat.android.core.ExperimentalStreamChatApi
import io.getstream.chat.android.models.Member
import io.getstream.chat.android.models.User
import io.getstream.chat.android.ui.common.utils.ExpandableList
import io.getstream.chat.android.ui.common.utils.emptyExpandableList

/**
 * Represents the state of the channel information in the UI.
 *
 * This sealed interface is used to model the different states that the channel information
 * can be in, such as loading or displaying content.
 */
@ExperimentalStreamChatApi
public sealed interface ChannelInfoViewState {

    /**
     * Represents the loading state of the channel information.
     */
    public data object Loading : ChannelInfoViewState

    /**
     * Represents the content state of the channel information.
     *
     * @param owner The owner of the channel.
     * @param members The list of members in the channel.
     * @param options The list of options available in the channel information UI.
     */
    public data class Content(
        val owner: User = User(),
        val members: ExpandableList<Member> = emptyExpandableList(),
        val options: List<Option> = emptyList(),
    ) : ChannelInfoViewState {

        /**
         * Represents the options available in the channel information UI.
         */
        public sealed interface Option {
            /**
             * Represents a separator option in the channel information UI.
             */
            public data object Separator : Option

            /**
             * Represents an option to add a member to the channel.
             */
            public data object AddMember : Option

            /**
             * Represents an option with user information.
             *
             * @param username The username.
             */
            public data class UserInfo(val username: String) : Option

            /**
             * Represents an option to rename the channel.
             *
             * @param name The current name of the channel.
             * @param isReadOnly Indicates if the channel is read-only.
             */
            public data class RenameChannel(val name: String, val isReadOnly: Boolean) : Option

            /**
             * Represents an option to mute the channel.
             *
             * @param isMuted Indicates if the channel is muted.
             */
            public data class MuteChannel(val isMuted: Boolean) : Option

            /**
             * Represents an option to hide the channel.
             *
             * @param isHidden Indicates if the channel is hidden.
             */
            public data class HideChannel(val isHidden: Boolean) : Option

            /**
             * Represents an option to view pinned messages in the channel.
             */
            public data object PinnedMessages : Option

            /**
             * Represents an option to leave the channel.
             */
            public data object LeaveChannel : Option

            /**
             * Represents an option to delete the channel.
             */
            public data object DeleteChannel : Option
        }
    }
}
