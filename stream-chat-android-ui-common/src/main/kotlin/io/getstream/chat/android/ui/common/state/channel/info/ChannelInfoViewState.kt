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

import io.getstream.chat.android.models.User

public data class ChannelInfoViewState(
    val content: Content = Content.Loading,
) {

    public sealed interface Content {
        public data object Loading : Content

        public data class Success(
            val expandedMembers: List<Member> = emptyList(),
            val collapsedMembers: List<Member> = emptyList(),
            val areMembersExpandable: Boolean = false,
            val areMembersExpanded: Boolean = false,
            val name: String = "",
            val isMuted: Boolean = false,
        ) : Content

        public data class Error(val message: String) : Content
    }

    public data class Member(
        val user: User,
        val role: Role,
    )

    public sealed interface Role {
        public data object Owner : Role
        public data object Moderator : Role
        public data object Member : Role
        public data class Other(val value: String) : Role
    }
}
