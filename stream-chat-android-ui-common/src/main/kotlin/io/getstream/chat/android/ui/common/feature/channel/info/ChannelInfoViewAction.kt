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
import io.getstream.chat.android.models.Message
import io.getstream.chat.android.models.User

/**
 * Represents actions that can be performed from the channel information view.
 */
public sealed interface ChannelInfoViewAction {
    /**
     * Represents the expand list of members click action.
     */
    public data object ExpandMembersClick : ChannelInfoViewAction

    /**
     * Represents the collapse list of members click action.
     */
    public data object CollapseMembersClick : ChannelInfoViewAction

    /**
     * Represents the click action on a member.
     *
     * @param member The member that was clicked.
     */
    public data class MemberClick(val member: Member) : ChannelInfoViewAction

    /**
     * Represents the user info click action.
     *
     * @param user The clicked user.
     */
    public data class UserInfoClick(val user: User) : ChannelInfoViewAction

    /**
     * Represents the rename channel click action.
     *
     * @param name The new name for the channel.
     */
    public data class RenameChannelClick(val name: String) : ChannelInfoViewAction

    /**
     * Represents the pinned messages click action.
     */
    public data object PinnedMessagesClick : ChannelInfoViewAction

    /**
     * Represents the media attachments click action.
     */
    public data object MediaAttachmentsClick : ChannelInfoViewAction

    /**
     * Represents the files attachments click action.
     */
    public data object FilesAttachmentsClick : ChannelInfoViewAction

    /**
     * Represents the mute channel click action.
     */
    public data object MuteChannelClick : ChannelInfoViewAction

    /**
     * Represents the unmute channel click action.
     */
    public data object UnmuteChannelClick : ChannelInfoViewAction

    /**
     * Represents the hide channel click action.
     */
    public data object HideChannelClick : ChannelInfoViewAction

    /**
     * Represents the confirmation click action for hiding a channel.
     *
     * @param clearHistory Whether to clear the channel history when hiding.
     */
    public data class HideChannelConfirmationClick(val clearHistory: Boolean) : ChannelInfoViewAction

    /**
     * Represents the unhide channel click action.
     */
    public data object UnhideChannelClick : ChannelInfoViewAction

    /**
     * Represents the leave channel click action.
     */
    public data object LeaveChannelClick : ChannelInfoViewAction

    /**
     * Represents the confirmation click action for leaving a channel.
     *
     * @param quitMessage The system message to send when leaving the channel. Defaults to null.
     */
    public data class LeaveChannelConfirmationClick(val quitMessage: Message?) : ChannelInfoViewAction

    /**
     * Represents the delete channel click action.
     */
    public data object DeleteChannelClick : ChannelInfoViewAction

    /**
     * Represents the confirmation click action for deleting a channel.
     */
    public data object DeleteChannelConfirmationClick : ChannelInfoViewAction

    /**
     * Represents the confirmation click action to ban the member.
     *
     * @param memberId The ID of the member to be banned.
     * @param timeoutInMinutes The duration for which the member should be banned, in minutes. Null for no timeout.
     */
    public data class BanMemberConfirmationClick(
        val memberId: String,
        val timeoutInMinutes: Int?,
    ) : ChannelInfoViewAction

    /**
     * Represents the confirmation click action to remove the member.
     *
     * @param memberId The ID of the member to be removed.
     */
    public data class RemoveMemberConfirmationClick(val memberId: String) : ChannelInfoViewAction
}
