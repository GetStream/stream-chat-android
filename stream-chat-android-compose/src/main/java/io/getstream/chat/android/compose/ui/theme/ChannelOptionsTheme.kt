/*
 * Copyright (c) 2014-2024 Stream.io Inc. All rights reserved.
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

package io.getstream.chat.android.compose.ui.theme

import androidx.compose.runtime.Immutable
import io.getstream.chat.android.compose.ui.components.channels.ChannelOptionItemVisibility

/**
 * Represents the theme for the channel option list in the selected channel menu.
 *
 * @param optionVisibility The visibility of the channel options.
 *
 * @see ChannelOptionItemVisibility
 */
@Immutable
public data class ChannelOptionsTheme(
    public val optionVisibility: ChannelOptionItemVisibility,
) {
    public companion object {
        public fun defaultTheme(
            optionVisibility: ChannelOptionItemVisibility = ChannelOptionItemVisibility(),
        ): ChannelOptionsTheme = ChannelOptionsTheme(
            optionVisibility = optionVisibility,
        )
    }
}
