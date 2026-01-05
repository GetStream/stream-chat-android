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

package io.getstream.chat.android.compose.state

import androidx.compose.ui.Alignment

/**
 * Represents the position of the online indicator in user avatars.
 *
 * @param alignment The standard Compose [Alignment] that corresponds to the indicator alignment.
 */
public enum class OnlineIndicatorAlignment(public val alignment: Alignment) {
    /**
     * The top end position within the avatar.
     */
    TopEnd(Alignment.TopEnd),

    /**
     * The bottom end position within the avatar.
     */
    BottomEnd(Alignment.BottomEnd),

    /**
     * The top start position within the avatar.
     */
    TopStart(Alignment.TopStart),

    /**
     * The bottom start position within the avatar.
     */
    BottomStart(Alignment.BottomStart),
}
