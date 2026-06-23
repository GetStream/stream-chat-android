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

package io.getstream.chat.android.ui.common.state.channel.info

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
         * Whether the conversation is muted: the channel is muted for a group, or the channel or
         * the other user is muted for a direct message.
         */
        public val isMuted: Boolean
            get() = options.any { option ->
                (option is Option.MuteChannel && option.isMuted) ||
                    (option is Option.MuteUser && option.isMuted)
            }

        /**
         * Represents the options available in the channel information UI.
         */
        public sealed interface Option {
            /**
             * Indicates an option to add a member to the channel.
             */
            public data object AddMember : Option

            /**
             * Indicates an option to edit the channel (name, image, etc.).
             *
             * @param name The current name of the channel.
             */
            public data class EditChannel(val name: String) : Option

            /**
             * Indicates an option to mute the channel.
             *
             * @param isMuted Indicates if the channel is muted.
             */
            public data class MuteChannel(val isMuted: Boolean) : Option

            /**
             * Indicates an option to mute the other user in a direct channel.
             *
             * @param isMuted Indicates if the user is muted.
             */
            public data class MuteUser(val isMuted: Boolean) : Option

            /**
             * Indicates an option to block the other user in a direct channel.
             *
             * @param isBlocked Indicates if the user is blocked.
             */
            public data class BlockUser(val isBlocked: Boolean) : Option

            /**
             * Indicates an option to view the channel's pinned messages.
             */
            public data object PinnedMessages : Option

            /**
             * Indicates an option to view the channel's media attachments.
             */
            public data object MediaAttachments : Option

            /**
             * Indicates an option to view the channel's files attachments.
             */
            public data object FilesAttachments : Option

            /**
             * Indicates an option to leave the channel.
             */
            public data object LeaveChannel : Option

            /**
             * Indicates an option to delete the channel.
             */
            public data object DeleteChannel : Option
        }
    }
}
