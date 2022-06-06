/*
 * Copyright (c) 2014-2022 Stream.io Inc. All rights reserved.
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

package io.getstream.chat.android.compose.ui.components.avatar

import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import io.getstream.chat.android.compose.ui.theme.ChatTheme

/**
 * Determines the offset for the initials of the [InitialsAvatar] based on their position inside the group.
 *
 * @param initialsOffset The offset to be applied to the initials.
 */
public sealed class InitialsAvatarOffset(internal val initialsOffset: @Composable () -> DpOffset) {
    /**
     * Notifies that the initials are in the top start position and applies the offset accordingly.
     */
    public object TopStart : InitialsAvatarOffset({
        DpOffset(
            ChatTheme.dimens.groupAvatarInitialsXOffset,
            ChatTheme.dimens.groupAvatarInitialsYOffset
        )
    })
    /**
     * Notifies that the initials are in the top end position and applies the offset accordingly.
     */
    public object TopEnd : InitialsAvatarOffset({
        DpOffset(
            -ChatTheme.dimens.groupAvatarInitialsXOffset,
            ChatTheme.dimens.groupAvatarInitialsYOffset
        )
    })

    /**
     * Notifies that the initials are in the bottom start position and applies the offset accordingly.
     */
    public object BottomStart : InitialsAvatarOffset({
        DpOffset(
            ChatTheme.dimens.groupAvatarInitialsXOffset,
            -ChatTheme.dimens.groupAvatarInitialsYOffset
        )
    })

    /**
     * Notifies that the initials are in the bottom end position and applies the offset accordingly.
     */
    public object BottomEnd : InitialsAvatarOffset({
        DpOffset(
            -ChatTheme.dimens.groupAvatarInitialsXOffset,
            -ChatTheme.dimens.groupAvatarInitialsYOffset
        )
    })

    /**
     * Notifies that the initials should be in the center of the avatar without any offset.
     */
    public object Center : InitialsAvatarOffset({
        DpOffset(0.dp, 0.dp)
    })
}
