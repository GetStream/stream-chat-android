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

package io.getstream.chat.android.compose.ui.chats

import java.io.Serializable

/**
 * The mode for displaying the list content in the chat screen.
 */
public enum class ListContentMode {
    /**
     * Display the list of channels.
     */
    Channels,

    /**
     * Display the list of threads.
     */
    Threads,
}

/**
 * The mode for displaying info content in the chat screen.
 *
 * Implements [Serializable] to allow for saving and restoring the state across configuration changes.
 *
 * @param id The ID of the content to display.
 */
public sealed class InfoContentMode(public open val id: String) : Serializable {
    /**
     * No info content.
     */
    public data object Hidden : InfoContentMode("")

    /**
     * Display the info for a single channel.
     */
    public data class SingleChannelInfo(override val id: String) : InfoContentMode(id)

    /**
     * Display the info for a group of channels.
     */
    public data class GroupChannelInfo(override val id: String) : InfoContentMode(id)
}
