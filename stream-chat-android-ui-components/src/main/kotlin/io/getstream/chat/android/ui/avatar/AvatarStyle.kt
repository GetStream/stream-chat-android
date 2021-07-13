package io.getstream.chat.android.ui.avatar

import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.util.AttributeSet
import androidx.annotation.ColorInt
import androidx.annotation.Px
import io.getstream.chat.android.ui.R
import io.getstream.chat.android.ui.TransformStyle
import io.getstream.chat.android.ui.common.extensions.internal.getDimension
import io.getstream.chat.android.ui.common.extensions.internal.getEnum
import io.getstream.chat.android.ui.common.extensions.internal.use
import io.getstream.chat.android.ui.common.style.TextStyle

public data class AvatarStyle(
    @Px public val avatarBorderWidth: Int,
    @ColorInt public val avatarBorderColor: Int,
    public val avatarInitialText: TextStyle,
    public val onlineIndicatorEnabled: Boolean,
    public val onlineIndicatorPosition: AvatarView.OnlineIndicatorPosition,
    @ColorInt public val onlineIndicatorColor: Int,
    @ColorInt public val onlineIndicatorBorderColor: Int,
) {

    internal companion object {
        operator fun invoke(context: Context, attrs: AttributeSet?): AvatarStyle {
            context.obtainStyledAttributes(
                attrs,
                R.styleable.AvatarView,
                0,
                0
            ).use {
                val avatarBorderWidth = it.getDimensionPixelSize(
                    R.styleable.AvatarView_streamUiAvatarBorderWidth,
                    context.getDimension(R.dimen.stream_ui_avatar_border_width)
                )
                val avatarBorderColor = it.getColor(
                    R.styleable.AvatarView_streamUiAvatarBorderColor,
                    Color.WHITE
                )
                val avatarInitialText = TextStyle.Builder(it)
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
                val onlineIndicatorEnabled = it.getBoolean(
                    R.styleable.AvatarView_streamUiAvatarOnlineIndicatorEnabled,
                    false
                )
                val onlineIndicatorPosition = it.getEnum(
                    R.styleable.AvatarView_streamUiAvatarOnlineIndicatorPosition,
                    AvatarView.OnlineIndicatorPosition.TOP_RIGHT
                )
                val onlineIndicatorColor = it.getColor(R.styleable.AvatarView_streamUiAvatarOnlineIndicatorColor, Color.GREEN)
                val onlineIndicatorBorderColor = it.getColor(R.styleable.AvatarView_streamUiAvatarOnlineIndicatorBorderColor, Color.WHITE)
                return AvatarStyle(
                    avatarBorderWidth = avatarBorderWidth,
                    avatarBorderColor = avatarBorderColor,
                    avatarInitialText = avatarInitialText,
                    onlineIndicatorEnabled = onlineIndicatorEnabled,
                    onlineIndicatorPosition = onlineIndicatorPosition,
                    onlineIndicatorColor = onlineIndicatorColor,
                    onlineIndicatorBorderColor = onlineIndicatorBorderColor,
                ).let(TransformStyle.avatarStyleTransformer::transform)
            }
        }
    }
}
