package io.getstream.chat.android.ui.avatar

import android.content.Context
import android.content.res.TypedArray
import android.graphics.Color
import android.graphics.Typeface
import android.util.AttributeSet
import com.getstream.sdk.chat.style.TextStyle
import io.getstream.chat.android.ui.R
import io.getstream.chat.android.ui.utils.getDimension

public class AvatarStyle(context: Context, attrs: AttributeSet?) {
    public val avatarWidth: Int
    public val avatarHeight: Int
    public val avatarBorderWidth: Int
    public val avatarBorderColor: Int
    public val avatarInitialText: TextStyle
    public val onlineIndicatorEnabled: Boolean

    init {
        val a: TypedArray = context.obtainStyledAttributes(
            attrs,
            R.styleable.AvatarView,
            0,
            0
        )

        avatarWidth = a.getDimensionPixelSize(
            R.styleable.AvatarView_streamAvatarWidth,
            context.getDimension(R.dimen.stream_message_avatar_width)
        )
        avatarHeight = a.getDimensionPixelSize(
            R.styleable.AvatarView_streamAvatarHeight,
            context.getDimension(R.dimen.stream_message_avatar_height)
        )
        avatarBorderWidth = a.getDimensionPixelSize(
            R.styleable.AvatarView_streamAvatarBorderWidth,
            context.getDimension(R.dimen.stream_channel_avatar_border_width)
        )
        avatarBorderColor = a.getColor(
            R.styleable.AvatarView_streamAvatarBorderColor,
            Color.WHITE
        )
        avatarInitialText = TextStyle.Builder(a)
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
        onlineIndicatorEnabled = a.getBoolean(
            R.styleable.AvatarView_streamAvatarOnlineIndicatorEnabled,
            false
        )
    }
}
