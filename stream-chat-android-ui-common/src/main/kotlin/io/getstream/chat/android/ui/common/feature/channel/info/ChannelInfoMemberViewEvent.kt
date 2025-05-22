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

package io.getstream.chat.android.ui.common.feature.channel.info

import io.getstream.chat.android.core.ExperimentalStreamChatApi
import io.getstream.chat.android.models.Member

/**
 * Represents side-effect events related to channel member information actions.
 */
@ExperimentalStreamChatApi
public sealed interface ChannelInfoMemberViewEvent {

    /**
     * Represents modal navigation events.
     */
    public sealed interface Modal : ChannelInfoMemberViewEvent

    /**
     * Indicates an event to present a modal for banning a member.
     *
     * @param member The member to be banned.
     */
    public data class BanMemberModal(val member: Member) : Modal {

        /**
         * The available timeout options for banning the member.
         */
        val timeouts: List<Timeout> = ChannelInfoMemberViewEvent.BanMemberModal.Timeout.entries.toList()

        /**
         * Represents the available timeout options for banning a member.
         *
         * @param valueInMinutes The duration for which the member should be banned, in minutes. Null for no timeout.
         */
        @Suppress("MagicNumber")
        public enum class Timeout(public val valueInMinutes: Int?) {
            /**
             * Indicates a timeout of 1 hour for the ban.
             */
            OneHour(60),

            /**
             * Indicates a timeout of 1 day for the ban.
             */
            OneDay(1440),

            /**
             * Indicates a timeout of 1 week for the ban.
             */
            OneWeek(10080),

            /**
             * Indicates no timeout for the ban.
             */
            NoTimeout(null),
        }
    }

    /**
     * Indicates an event to present a modal for removing a member.
     *
     * @param member The member to be removed.
     */
    public data class RemoveMemberModal(val member: Member) : Modal

    /**
     * Represents navigation events.
     *
     * @param reason The reason for navigation or null if not applicable.
     */
    public sealed class Navigation(public open val reason: Reason?) : ChannelInfoMemberViewEvent {
        /**
         * Represents the reason for navigation.
         */
        public sealed interface Reason
    }

    /**
     * Indicates an event to navigate to the channel.
     */
    public data class NavigateToChannel(val channelId: String) : Navigation(reason = null)

    /**
     * Represents error events occurred while performing an action.
     */
    public sealed interface Error : ChannelInfoMemberViewEvent

    /**
     * Indicates an error occurred while banning a member.
     */
    public data object BanMemberError : Error

    /**
     * Indicates an error occurred while unbanning a member.
     */
    public data object UnbanMemberError : Error

    /**
     * Indicates an error occurred while removing a member.
     */
    public data object RemoveMemberError : Error
}
