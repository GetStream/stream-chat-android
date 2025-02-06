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

package io.getstream.chat.android.ui.common.model

/**
 * The user presence display configuration for the current user and other users.
 *
 * @param currentUser The configuration for the current user.
 * @param otherUsers The configuration for other users.
 */
public data class UserPresence(
    val currentUser: DisplayOptions = DisplayOptions(),
    val otherUsers: DisplayOptions = DisplayOptions(),
) {

    /**
     * The display options of the user presence.
     *
     * @param showOnlineIndicator If the online indicator should be shown for the user.
     * @param countAsOnlineMember If the user should be counted as an online member.
     */
    public data class DisplayOptions(
        val showOnlineIndicator: Boolean = true,
        val countAsOnlineMember: Boolean = true,
    )
}
