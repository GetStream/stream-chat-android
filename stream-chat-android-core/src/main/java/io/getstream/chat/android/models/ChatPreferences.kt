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

package io.getstream.chat.android.models

import androidx.compose.runtime.Immutable

/**
 * Per-category push toggles. A null field means "use the server default". When set on a
 * [PushPreference], takes precedence over [PushPreference.level].
 */
@Immutable
public data class ChatPreferences(
    public val directMentions: ChatPreferenceToggle? = null,
    public val roleMentions: ChatPreferenceToggle? = null,
    public val groupMentions: ChatPreferenceToggle? = null,
    public val hereMentions: ChatPreferenceToggle? = null,
    public val channelMentions: ChatPreferenceToggle? = null,
    public val threadReplies: ChatPreferenceToggle? = null,
    public val defaultPreference: ChatPreferenceToggle? = null,
)

/**
 * Per-category toggle for [ChatPreferences]. Wraps the raw wire string so unknown values
 * round-trip without being lost.
 */
@Immutable
public data class ChatPreferenceToggle(public val value: String) {

    public companion object {

        /** Receive push for this category. */
        public val all: ChatPreferenceToggle = ChatPreferenceToggle(value = "all")

        /** Suppress push for this category. */
        public val none: ChatPreferenceToggle = ChatPreferenceToggle(value = "none")

        public fun fromValue(value: String?): ChatPreferenceToggle? = when (value) {
            null, "" -> null
            all.value -> all
            none.value -> none
            else -> ChatPreferenceToggle(value)
        }
    }
}
