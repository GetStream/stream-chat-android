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
import coil.ImageLoader
import coil.decode.DataSource
import coil.fetch.DrawableResult
import coil.fetch.Fetcher
import coil.request.Options
import coil.size.pxOrElse
import io.getstream.chat.android.client.extensions.getUsersExcludingCurrent
import io.getstream.chat.android.ui.ChatUI
import io.getstream.chat.android.ui.avatar.internal.Avatar

internal class AvatarFetcherFactory(
    private val excludeCurrentUserFromChannelAvatars: Boolean = false,
) : Fetcher.Factory<Avatar> {

    override fun create(data: Avatar, options: Options, imageLoader: ImageLoader): Fetcher {
        val targetSize = options.size.width.pxOrElse { 0 }
        val resources = options.context.resources
        return Fetcher {
            DrawableResult(
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
                            val users = if (excludeCurrentUserFromChannelAvatars) {
                                data.channel.getUsersExcludingCurrent()
                            } else {
                                data.channel.members.map { it.user }
                            }

                            ChatUI.avatarBitmapFactory.createChannelBitmapInternal(
                                data.channel,
                                users,
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
    }
}
