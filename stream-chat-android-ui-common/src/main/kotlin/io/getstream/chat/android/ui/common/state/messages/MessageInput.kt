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

package io.getstream.chat.android.ui.common.state.messages

/**
 * The input for a new message
 */
public data class MessageInput(
    val text: String = "",
    val source: Source = Source.Default,
) {

    public sealed class Source {
        /**
         * The initial value, used when the source is not specified
         */
        public data object Default : Source()

        /**
         * The message was created by the user
         */
        public data object External : Source()

        public sealed class Internal : Source()

        /**
         * The message was created by the user
         */
        public data object Edit : Internal()

        /**
         * The message was created internally by the SDK
         */
        public data object CommandSelected : Internal()

        /**
         * The message was created internally by the SDK
         */
        public data object MentionSelected : Internal()

        public data object DraftMessage : Internal()
    }
}
