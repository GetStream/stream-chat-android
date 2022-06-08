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

package io.getstream.chat.android.compose.ui.util.extensions.internal

import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import io.getstream.chat.android.compose.ui.theme.StreamDimens

/**
 * @return The x and y offset of the avatar inside [DpOffset] depending on the item position inside the list.
 */
internal fun getAvatarPositionOffset(
    dimens: StreamDimens,
    userPosition: Int,
    memberCount: Int,
): DpOffset {
    val center = DpOffset(0.dp, 0.dp)
    if (memberCount <= 2) return center

    return when (userPosition) {
        0 -> DpOffset(
            dimens.groupAvatarInitialsXOffset,
            dimens.groupAvatarInitialsYOffset
        )
        1 -> {
            if (memberCount == MaxSizeWithFullHeightAvatar) {
                center
            } else {
                DpOffset(
                    -dimens.groupAvatarInitialsXOffset,
                    dimens.groupAvatarInitialsYOffset
                )
            }
        }
        2 -> DpOffset(
            dimens.groupAvatarInitialsXOffset,
            -dimens.groupAvatarInitialsYOffset
        )
        LastIndexInAvatarGroup -> DpOffset(
            -dimens.groupAvatarInitialsXOffset,
            -dimens.groupAvatarInitialsYOffset
        )
        else -> center
    }
}

/**
 * If there are 3 avatars, the second one will have full height and doesn't need to be offset.
 */
private const val MaxSizeWithFullHeightAvatar = 3

/**
 * The last possible index of the avatars list.
 */
private const val LastIndexInAvatarGroup = 3
