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

/**
 * Represents the state of the channel member information in the UI.
 *
 * This sealed interface is used to model the different states that the channel member information
 * can be in, such as loading or displaying content.
 */
@ExperimentalStreamChatApi
public sealed interface ChannelInfoMemberViewState {

    /**
     * Represents the loading state of the channel member information.
     */
    public data object Loading : ChannelInfoMemberViewState

    /**
     * Represents the content state of the channel member information.
     *
     * @param member The member whose information is being displayed.
     * @param options The list of options available for the member.
     */
    public data class Content(
        val member: Member,
        val options: List<Option>,
    ) : ChannelInfoMemberViewState {

        /**
         * Represents the options available for the member.
         */
        public sealed interface Option {
            /**
             * Indicates an option to message the member.
             *
             * @param member The member to message.
             */
            public data class MessageMember(val member: Member) : Option

            /**
             * Indicates an option to ban the member.
             *
             * @param member The member to ban.
             */
            public data class BanMember(val member: Member) : Option

            /**
             * Indicates an option to unban the member.
             *
             * @param member The member to unban.
             */
            public data class UnbanMember(val member: Member) : Option

            /**
             * Indicates an option to remove the member from the channel.
             *
             * @param member The member to remove.
             */
            public data class RemoveMember(val member: Member) : Option
        }
    }
}
