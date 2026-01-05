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

package io.getstream.chat.android.ui.common.feature.channel.info

import io.getstream.chat.android.models.Member

/**
 * Represents side-effect events related to channel information actions.
 */
public sealed interface ChannelInfoViewEvent {

    /**
     * Represents modal navigation events.
     */
    public sealed interface Modal : ChannelInfoViewEvent

    /**
     * Indicates an event to present a modal for hiding a channel.
     */
    public data object HideChannelModal : Modal

    /**
     * Indicates an event to present a modal for leaving a channel.
     */
    public data object LeaveChannelModal : Modal

    /**
     * Indicates an event to present a modal for deleting a channel.
     */
    public data object DeleteChannelModal : Modal

    /**
     * Indicates an event to present a member information modal.
     *
     * @param cid The full channel identifier (e.g., "messaging:123").
     * @param member The member whose information is to be displayed.
     */
    public data class MemberInfoModal(
        val cid: String,
        val member: Member,
    ) : Modal

    /**
     * Indicates an event to present a modal for banning a member.
     *
     * @param member The member to be banned.
     */
    public data class BanMemberModal(val member: Member) : Modal {

        /**
         * The available timeout options for banning the member.
         */
        val timeouts: List<Timeout> = Timeout.entries.toList()

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
    public sealed class Navigation(public open val reason: Reason?) : ChannelInfoViewEvent {
        /**
         * Represents the reason for navigation.
         */
        public sealed interface Reason {
            /**
             * Indicates the channel was successfully hidden.
             */
            public data object HideChannelSuccess : Reason

            /**
             * Indicates the user successfully left the channel.
             */
            public data object LeaveChannelSuccess : Reason

            /**
             * Indicates the channel was successfully deleted.
             */
            public data object DeleteChannelSuccess : Reason
        }
    }

    /**
     * Indicates an event to navigate up in the view hierarchy.
     *
     * @param reason The reason for navigation.
     */
    public data class NavigateUp(override val reason: Reason) : Navigation(reason)

    /**
     * Indicates an event to navigate to the pinned messages.
     */
    public data object NavigateToPinnedMessages : Navigation(reason = null)

    /**
     * Indicates an event to navigate to the media attachments.
     */
    public data object NavigateToMediaAttachments : Navigation(reason = null)

    /**
     * Indicates an event to navigate to the files attachments.
     */
    public data object NavigateToFilesAttachments : Navigation(reason = null)

    /**
     * Indicates an event to navigate to the channel with the specified [cid].
     *
     * @param cid The full channel ID of the channel to navigate to.
     */
    public data class NavigateToChannel(val cid: String) : Navigation(reason = null)

    /**
     * Indicates an event to navigate to draft a channel with the specified [memberId].
     *
     * @param memberId The ID of the member to whom the draft channel belongs.
     */
    public data class NavigateToDraftChannel(val memberId: String) : Navigation(reason = null)

    /**
     * Represents error events occurred while performing an action.
     */
    public sealed interface Error : ChannelInfoViewEvent

    /**
     * Indicates an error occurred while renaming a channel.
     */
    public data object RenameChannelError : Error

    /**
     * Indicates an error occurred while muting a channel.
     */
    public data object MuteChannelError : Error

    /**
     * Indicates an error occurred while unmuting a channel.
     */
    public data object UnmuteChannelError : Error

    /**
     * Indicates an error occurred while hiding a channel.
     */
    public data object HideChannelError : Error

    /**
     * Indicates an error occurred while unhiding a channel.
     */
    public data object UnhideChannelError : Error

    /**
     * Indicates an error occurred while leaving a channel.
     */
    public data object LeaveChannelError : Error

    /**
     * Indicates an error occurred while deleting a channel.
     */
    public data object DeleteChannelError : Error

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
