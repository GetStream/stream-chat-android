package io.getstream.chat.android.ui.avatar.internal

import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.util.AttributeSet
import com.getstream.sdk.chat.style.TextStyle
import io.getstream.chat.android.ui.R
import io.getstream.chat.android.ui.avatar.AvatarView
import io.getstream.chat.android.ui.common.extensions.internal.getDimension
import io.getstream.chat.android.ui.common.extensions.internal.getEnum
import io.getstream.chat.android.ui.common.extensions.internal.use

internal class AvatarStyle internal constructor(context: Context, attrs: AttributeSet?) {
    var avatarBorderWidth: Int
        internal set
    var avatarBorderColor: Int
        internal set
    var avatarInitialText: TextStyle
        internal set
    var onlineIndicatorEnabled: Boolean
        internal set
    var onlineIndicatorPosition: AvatarView.OnlineIndicatorPosition
        internal set

    init {
        context.obtainStyledAttributes(
            attrs,
            R.styleable.AvatarView,
            0,
            0
        ).use {
            avatarBorderWidth = it.getDimensionPixelSize(
                R.styleable.AvatarView_streamUiAvatarBorderWidth,
                context.getDimension(R.dimen.stream_ui_avatar_border_width)
            )
            avatarBorderColor = it.getColor(
                R.styleable.AvatarView_streamUiAvatarBorderColor,
                Color.WHITE
            )
            avatarInitialText = TextStyle.Builder(it)
                .size(
                    R.styleable.AvatarView_streamUiAvatarTextSize,
                    context.getDimension(R.dimen.stream_ui_avatar_initials)
                )
                .color(
                    R.styleable.AvatarView_streamUiAvatarTextColor,
                    Color.WHITE
                )
                .font(
                    R.styleable.AvatarView_streamUiAvatarTextFontAssets,
                    R.styleable.AvatarView_streamUiAvatarTextFont
                )
                .style(
                    R.styleable.AvatarView_streamUiAvatarTextStyle,
                    Typeface.BOLD
                )
                .build()
            onlineIndicatorEnabled = it.getBoolean(
                R.styleable.AvatarView_streamUiAvatarOnlineIndicatorEnabled,
                false
            )
            onlineIndicatorPosition = it.getEnum(
                R.styleable.AvatarView_streamUiAvatarOnlineIndicatorPosition,
                AvatarView.OnlineIndicatorPosition.TOP
            )
        }
    }
}
