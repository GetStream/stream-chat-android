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

package io.getstream.chat.android.compose.sample.ui.chats

import io.getstream.chat.android.compose.ui.chats.ChatsScreen
import java.io.Serializable

/**
 * The mode for displaying info content in the [ChatsScreen].
 *
 * Implements [Serializable] to allow for saving and restoring the state across configuration changes.
 *
 * @param channelId The ID of the channel to display the info for.
 */
@Suppress("SerialVersionUIDInSerializableClass")
sealed class InfoContentMode(open val channelId: String) : Serializable {
    /**
     * No info content.
     */
    data object Hidden : InfoContentMode("")

    /**
     * Display the info for a single channel.
     */
    data class SingleChannelInfo(override val channelId: String) : InfoContentMode(channelId)

    /**
     * Display the info for a group channel.
     */
    data class GroupChannelInfo(override val channelId: String) : InfoContentMode(channelId)
}
