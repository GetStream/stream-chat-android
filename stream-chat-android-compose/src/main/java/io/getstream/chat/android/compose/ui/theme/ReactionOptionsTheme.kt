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

package io.getstream.chat.android.compose.ui.theme

import androidx.compose.runtime.Immutable

/**
 * Represents the theme for the reaction option list in the selected message menu.
 * For message option list theming, see [MessageOptionsTheme].
 *
 * @param areReactionOptionsVisible The visibility of the reaction options.
 */
@Immutable
public data class ReactionOptionsTheme(
    public val areReactionOptionsVisible: Boolean,
) {
    public companion object {
        public fun defaultTheme(
            areReactionOptionsVisible: Boolean = true,
        ): ReactionOptionsTheme = ReactionOptionsTheme(
            areReactionOptionsVisible = areReactionOptionsVisible,
        )
    }
}
