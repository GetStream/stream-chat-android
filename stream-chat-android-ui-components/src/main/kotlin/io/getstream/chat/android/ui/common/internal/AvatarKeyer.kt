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

package io.getstream.chat.android.ui.common.internal

import coil.key.Keyer
import coil.request.Options
import io.getstream.chat.android.ui.ChatUI
import io.getstream.chat.android.ui.avatar.internal.Avatar

internal object AvatarKeyer : Keyer<Avatar> {
    override fun key(data: Avatar, options: Options): String? = when (data) {
        is Avatar.UserAvatar -> ChatUI.avatarBitmapFactory.userBitmapKey(data.user)
        is Avatar.ChannelAvatar -> ChatUI.avatarBitmapFactory.channelBitmapKey(data.channel)
    }
}
