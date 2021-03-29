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
import com.getstream.sdk.chat.utils.extensions.getUsers
import io.getstream.chat.android.ui.avatar.AvatarBitmapFactory
import io.getstream.chat.android.ui.avatar.internal.Avatar

internal class AvatarFetcher() : Fetcher<Avatar> {

    override suspend fun fetch(
        pool: BitmapPool,
        data: Avatar,
        size: Size,
        options: Options
    ): FetchResult {
        val targetSize = size.let { if (it is PixelSize) it.width else 0 }
        val resources = options.context.resources
        return DrawableResult(
            BitmapDrawable(
                resources,
                when (data) {
                    is Avatar.UserAvatar -> {
                        AvatarBitmapFactory.instance.createUserBitmapInternal(
                            data.user,
                            data.avatarStyle,
                            targetSize
                        )
                    }
                    is Avatar.ChannelAvatar -> {
                        AvatarBitmapFactory.instance.createChannelBitmapInternal(
                            data.channel,
                            data.channel.getUsers(),
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
        is Avatar.UserAvatar -> AvatarBitmapFactory.instance.userBitmapKey(data.user)
        is Avatar.ChannelAvatar -> AvatarBitmapFactory.instance.channelBitmapKey(data.channel)
    }
}
