package io.getstream.chat.android.ui.avatar

import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.util.AttributeSet
import com.getstream.sdk.chat.style.TextStyle
import io.getstream.chat.android.ui.R
import io.getstream.chat.android.ui.utils.extensions.getDimension
import io.getstream.chat.android.ui.utils.extensions.getEnum
import io.getstream.chat.android.ui.utils.extensions.use

public class AvatarStyle internal constructor(context: Context, attrs: AttributeSet?) {
    public var avatarBorderWidth: Int
        internal set
    public var avatarBorderColor: Int
        internal set
    public var avatarInitialText: TextStyle
        internal set
    public var onlineIndicatorEnabled: Boolean
        internal set
    public var onlineIndicatorPosition: AvatarView.OnlineIndicatorPosition
        internal set

    init {
        context.obtainStyledAttributes(
            attrs,
            R.styleable.StreamUiAvatarView,
            0,
            0
        ).use {
            avatarBorderWidth = it.getDimensionPixelSize(
                R.styleable.StreamUiAvatarView_streamUiAvatarBorderWidth,
                context.getDimension(R.dimen.stream_channel_avatar_border_width)
            )
            avatarBorderColor = it.getColor(
                R.styleable.StreamUiAvatarView_streamUiAvatarBorderColor,
                Color.WHITE
            )
            avatarInitialText = TextStyle.Builder(it)
                .size(
                    R.styleable.StreamUiAvatarView_streamUiAvatarTextSize,
                    context.getDimension(R.dimen.stream_channel_initials)
                )
                .color(
                    R.styleable.StreamUiAvatarView_streamUiAvatarTextColor,
                    Color.WHITE
                )
                .font(
                    R.styleable.StreamUiAvatarView_streamUiAvatarTextFontAssets,
                    R.styleable.StreamUiAvatarView_streamUiAvatarTextFont
                )
                .style(
                    R.styleable.StreamUiAvatarView_streamUiAvatarTextStyle,
                    Typeface.BOLD
                )
                .build()
            onlineIndicatorEnabled = it.getBoolean(
                R.styleable.StreamUiAvatarView_streamUiAvatarOnlineIndicatorEnabled,
                false
            )
            onlineIndicatorPosition = it.getEnum(
                R.styleable.StreamUiAvatarView_streamUiAvatarOnlineIndicatorPosition,
                AvatarView.OnlineIndicatorPosition.TOP
            )
        }
    }
}
