package io.getstream.chat.android.ui.coil

import android.graphics.drawable.BitmapDrawable
import coil.bitmap.BitmapPool
import coil.decode.DataSource
import coil.decode.Options
import coil.fetch.DrawableResult
import coil.fetch.FetchResult
import coil.fetch.Fetcher
import coil.size.PixelSize
import coil.size.Size
import com.getstream.sdk.chat.utils.extensions.getUsers
import io.getstream.chat.android.client.models.image
import io.getstream.chat.android.client.models.name
import io.getstream.chat.android.ui.avatar.Avatar
import io.getstream.chat.android.ui.avatar.AvatarBitmapCombiner
import io.getstream.chat.android.ui.avatar.AvatarBitmapFactory

internal class AvatarFetcher(
    private val avatarBitmapFactory: AvatarBitmapFactory
) : Fetcher<Avatar> {

    override suspend fun fetch(
        pool: BitmapPool,
        data: Avatar,
        size: Size,
        options: Options
    ): FetchResult {
        val targetSize = size.let { if (it is PixelSize) it.width else 0 }
        val resources = options.context.resources
        return when (data) {
            is Avatar.UserAvatar -> {
                avatarBitmapFactory.createUserBitmap(
                    data.user,
                    data.avatarStyle,
                    targetSize
                ).let {
                    DrawableResult(BitmapDrawable(resources, it), false, DataSource.MEMORY)
                }
            }
            is Avatar.ChannelAvatar -> {
                avatarBitmapFactory.createChannelBitmaps(
                    data.channel,
                    data.channel.getUsers(),
                    data.avatarStyle,
                    targetSize
                ).let {
                    if (it.size == 1) it[0] else AvatarBitmapCombiner.combine(it, targetSize)
                }.let {
                    DrawableResult(BitmapDrawable(resources, it), false, DataSource.MEMORY)
                }
            }
        }
    }

    override fun key(data: Avatar): String? {

        return when (data) {
            is Avatar.UserAvatar -> {
                "${data.user.name}${data.user.image}"
            }
            is Avatar.ChannelAvatar -> {
                buildString {
                    append(data.channel.name)
                    append(data.channel.image)
                    data.channel.getUsers()
                        .take(4)
                        .forEach {
                            append(it.name)
                            append(it.image)
                        }
                }
            }
        }
    }
}
