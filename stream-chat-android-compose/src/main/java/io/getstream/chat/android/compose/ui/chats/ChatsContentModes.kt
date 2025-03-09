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

import androidx.compose.runtime.saveable.Saver

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
 * The mode for displaying extra content in the chat screen.
 */
public sealed class ExtraContentMode(public open val id: String) {
    /**
     * No extra content.
     */
    public data object Hidden : ExtraContentMode("")

    public data class SingleChannelInfo(override val id: String) : ExtraContentMode(id)

    public data class GroupChannelInfo(override val id: String) : ExtraContentMode(id)

    public companion object {

        /**
         * The default [Saver] implementation for [ExtraContentMode].
         */
        public val Saver: Saver<ExtraContentMode, String> = Saver(
            save = { mode ->
                when (mode) {
                    is SingleChannelInfo -> "SingleChannelInfo:${mode.id}"
                    is GroupChannelInfo -> "GroupChannelInfo:${mode.id}"
                    else -> ""
                }
            },
            restore = { state ->
                when {
                    state.startsWith("SingleChannelInfo:") -> {
                        val id = state.removePrefix("SingleChannelInfo:")
                        SingleChannelInfo(id)
                    }

                    state.startsWith("GroupChannelInfo:") -> {
                        val id = state.removePrefix("GroupChannelInfo:")
                        GroupChannelInfo(id)
                    }

                    else -> Hidden
                }
            },
        )
    }
}
