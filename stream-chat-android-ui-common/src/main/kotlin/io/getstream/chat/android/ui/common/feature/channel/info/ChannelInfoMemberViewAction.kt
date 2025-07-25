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

/**
 * Represents actions that can be performed from the channel member information view.
 */
public sealed interface ChannelInfoMemberViewAction {
    /**
     * Represents the click action to message the member.
     */
    public data object MessageMemberClick : ChannelInfoMemberViewAction

    /**
     * Represents the click action to ban the member.
     */
    public data object BanMemberClick : ChannelInfoMemberViewAction

    /**
     * Represents the click action to unban the member.
     */
    public data object UnbanMemberClick : ChannelInfoMemberViewAction

    /**
     * Represents the click action to remove the member.
     */
    public data object RemoveMemberClick : ChannelInfoMemberViewAction
}
