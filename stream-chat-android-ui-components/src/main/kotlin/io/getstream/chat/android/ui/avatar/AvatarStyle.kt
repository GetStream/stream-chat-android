package io.getstream.chat.android.ui.avatar

import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.util.AttributeSet
import com.getstream.sdk.chat.style.TextStyle
import io.getstream.chat.android.ui.R
import io.getstream.chat.android.ui.utils.extensions.getDimension
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

    init {
        context.obtainStyledAttributes(
            attrs,
            R.styleable.StreamAvatarView,
            0,
            0
        ).use {
            avatarBorderWidth = it.getDimensionPixelSize(
                R.styleable.StreamAvatarView_streamAvatarBorderWidth,
                context.getDimension(R.dimen.stream_channel_avatar_border_width)
            )
            avatarBorderColor = it.getColor(
                R.styleable.StreamAvatarView_streamAvatarBorderColor,
                Color.WHITE
            )
            avatarInitialText = TextStyle.Builder(it)
                .size(
                    R.styleable.StreamAvatarView_streamAvatarTextSize,
                    context.getDimension(R.dimen.stream_channel_initials)
                )
                .color(
                    R.styleable.StreamAvatarView_streamAvatarTextColor,
                    Color.WHITE
                )
                .font(
                    R.styleable.StreamAvatarView_streamAvatarTextFontAssets,
                    R.styleable.StreamAvatarView_streamAvatarTextFont
                )
                .style(
                    R.styleable.StreamAvatarView_streamAvatarTextStyle,
                    Typeface.BOLD
                )
                .build()
            onlineIndicatorEnabled = it.getBoolean(
                R.styleable.StreamAvatarView_streamAvatarOnlineIndicatorEnabled,
                false
            )
        }
    }
}
