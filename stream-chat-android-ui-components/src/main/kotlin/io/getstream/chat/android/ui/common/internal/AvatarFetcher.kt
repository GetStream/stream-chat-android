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

import android.graphics.drawable.BitmapDrawable
import coil.bitmap.BitmapPool
import coil.decode.DataSource
import coil.decode.Options
import coil.fetch.DrawableResult
import coil.fetch.FetchResult
import coil.fetch.Fetcher
import coil.size.PixelSize
import coil.size.Size
import io.getstream.chat.android.client.extensions.getUsersExcludingCurrent
import io.getstream.chat.android.ui.ChatUI
import io.getstream.chat.android.ui.avatar.internal.Avatar

internal class AvatarFetcher : Fetcher<Avatar> {

    override suspend fun fetch(
        pool: BitmapPool,
        data: Avatar,
        size: Size,
        options: Options,
    ): FetchResult {
        val targetSize = size.let { if (it is PixelSize) it.width else 0 }
        val resources = options.context.resources
        return DrawableResult(
            BitmapDrawable(
                resources,
                when (data) {
                    is Avatar.UserAvatar -> {
                        ChatUI.avatarBitmapFactory.createUserBitmapInternal(
                            data.user,
                            data.avatarStyle,
                            targetSize
                        )
                    }
                    is Avatar.ChannelAvatar -> {
                        ChatUI.avatarBitmapFactory.createChannelBitmapInternal(
                            data.channel,
                            data.channel.getUsersExcludingCurrent(),
                            data.avatarStyle,
                            targetSize
                        )
                    }
                }
            ),
            false,
            DataSource.MEMORY
        )
    }

    override fun key(data: Avatar): String? = when (data) {
        is Avatar.UserAvatar -> ChatUI.avatarBitmapFactory.userBitmapKey(data.user)
        is Avatar.ChannelAvatar -> ChatUI.avatarBitmapFactory.channelBitmapKey(data.channel)
    }
}
