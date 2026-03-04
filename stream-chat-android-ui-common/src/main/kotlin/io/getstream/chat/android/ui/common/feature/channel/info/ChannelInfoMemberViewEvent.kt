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
 * Represents side-effect events related to channel member information actions.
 */
public sealed interface ChannelInfoMemberViewEvent {

    /**
     * Indicates an event to proceed with messaging a member.
     *
     * @param memberId The ID of the member to message.
     * @param distinctCid The full distinct channel ID, if any.
     */
    public data class MessageMember(val memberId: String, val distinctCid: String?) : ChannelInfoMemberViewEvent

    /**
     * Indicates an event to proceed with banning a member.
     *
     * @param member The member to be banned.
     */
    public data class BanMember(val member: Member) : ChannelInfoMemberViewEvent

    /**
     * Indicates an event to proceed with unbanning a member.
     *
     * @param member The member to be unbanned.
     */
    public data class UnbanMember(val member: Member) : ChannelInfoMemberViewEvent

    /**
     * Indicates an event to proceed with muting a member's user.
     *
     * @param member The member whose user is to be muted.
     */
    public data class MuteUser(val member: Member) : ChannelInfoMemberViewEvent

    /**
     * Indicates an event to proceed with unmuting a member's user.
     *
     * @param member The member whose user is to be unmuted.
     */
    public data class UnmuteUser(val member: Member) : ChannelInfoMemberViewEvent

    /**
     * Indicates an event to proceed with blocking a member's user.
     *
     * @param member The member whose user is to be blocked.
     */
    public data class BlockUser(val member: Member) : ChannelInfoMemberViewEvent

    /**
     * Indicates an event to proceed with unblocking a member's user.
     *
     * @param member The member whose user is to be unblocked.
     */
    public data class UnblockUser(val member: Member) : ChannelInfoMemberViewEvent

    /**
     * Indicates an event to proceed with removing a member.
     *
     * @param member The member to be removed.
     */
    public data class RemoveMember(val member: Member) : ChannelInfoMemberViewEvent
}
