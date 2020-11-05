package io.getstream.chat.android.ui.avatar

import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.util.AttributeSet
import com.getstream.sdk.chat.style.TextStyle
import io.getstream.chat.android.ui.R
import io.getstream.chat.android.ui.utils.extensions.getDimension
import io.getstream.chat.android.ui.utils.extensions.use

internal class AvatarStyle(context: Context, attrs: AttributeSet?) {
    val avatarWidth: Int
    val avatarHeight: Int
    val avatarBorderWidth: Int
    val avatarBorderColor: Int
    val avatarInitialText: TextStyle
    val onlineIndicatorEnabled: Boolean

    init {
        context.obtainStyledAttributes(
            attrs,
            R.styleable.AvatarView,
            0,
            0
        ).use {
            avatarWidth = it.getDimensionPixelSize(
                R.styleable.AvatarView_streamAvatarWidth,
                context.getDimension(R.dimen.stream_message_avatar_width)
            )
            avatarHeight = it.getDimensionPixelSize(
                R.styleable.AvatarView_streamAvatarHeight,
                context.getDimension(R.dimen.stream_message_avatar_height)
            )
            avatarBorderWidth = it.getDimensionPixelSize(
                R.styleable.AvatarView_streamAvatarBorderWidth,
                context.getDimension(R.dimen.stream_channel_avatar_border_width)
            )
            avatarBorderColor = it.getColor(
                R.styleable.AvatarView_streamAvatarBorderColor,
                Color.WHITE
            )
            avatarInitialText = TextStyle.Builder(it)
                .size(
                    R.styleable.AvatarView_streamAvatarTextSize,
                    context.getDimension(R.dimen.stream_channel_initials)
                )
                .color(
                    R.styleable.AvatarView_streamAvatarTextColor,
                    Color.WHITE
                )
                .font(
                    R.styleable.AvatarView_streamAvatarTextFontAssets,
                    R.styleable.AvatarView_streamAvatarTextFont
                )
                .style(
                    R.styleable.AvatarView_streamAvatarTextStyle,
                    Typeface.BOLD
                )
                .build()
            onlineIndicatorEnabled = it.getBoolean(
                R.styleable.AvatarView_streamAvatarOnlineIndicatorEnabled,
                false
            )
        }
    }
}
