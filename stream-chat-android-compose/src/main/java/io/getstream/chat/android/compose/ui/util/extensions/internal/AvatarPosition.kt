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

import io.getstream.chat.android.compose.ui.components.avatar.InitialsAvatarOffset

/**
 * @return The [InitialsAvatarOffset] depending on the item index inside the list.
 */
internal fun Int.getAvatarPosition(listSize: Int): InitialsAvatarOffset {
    if (listSize <= 2) return InitialsAvatarOffset.Center

    return when (this) {
        0 -> InitialsAvatarOffset.TopStart
        1 -> if (listSize == MaxSizeWithFullHeightAvatar) InitialsAvatarOffset.Center else InitialsAvatarOffset.TopEnd
        2 -> InitialsAvatarOffset.BottomStart
        LastIndexInAvatarGroup -> InitialsAvatarOffset.BottomEnd
        else -> InitialsAvatarOffset.Center
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
