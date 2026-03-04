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

/**
 * Represents the state of the channel member information in the UI.
 *
 * This sealed interface is used to model the different states that the channel member information
 * can be in, such as loading or displaying content.
 */
public sealed interface ChannelInfoMemberViewState {

    /**
     * Represents the loading state of the channel member information.
     */
    public data object Loading : ChannelInfoMemberViewState

    /**
     * Represents the content state of the channel member information.
     *
     * @param member The member whose information is being displayed.
     * @param capabilities The set of own capabilities in the channel.
     * @param isMuted Whether the member's user is muted by the current user.
     * @param isBlocked Whether the member's user is blocked by the current user.
     */
    public data class Content(
        val member: Member,
        val capabilities: Set<String>,
        val isMuted: Boolean,
        val isBlocked: Boolean,
    ) : ChannelInfoMemberViewState
}
