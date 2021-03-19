package io.getstream.chat.android.ui.avatar

import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.util.AttributeSet
import androidx.annotation.ColorInt
import com.getstream.sdk.chat.style.TextStyle
import io.getstream.chat.android.ui.R
import io.getstream.chat.android.ui.common.extensions.internal.getDimension
import io.getstream.chat.android.ui.common.extensions.internal.getEnum

public data class AvatarStyle(
    public val avatarBorderWidth: Int,
    @ColorInt public val avatarBorderColor: Int,
    public val avatarInitialText: TextStyle,
    public val onlineIndicatorEnabled: Boolean,
    public val onlineIndicatorPosition: AvatarView.OnlineIndicatorPosition,
) {

    internal companion object {

        operator fun invoke(context: Context, attrs: AttributeSet?): AvatarStyle =
            context.obtainStyledAttributes(
                attrs,
                R.styleable.AvatarView,
                0,
                0
            ).let {
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
                    AvatarView.OnlineIndicatorPosition.TOP
                )
                AvatarStyle(
                    avatarBorderWidth = avatarBorderWidth,
                    avatarBorderColor = avatarBorderColor,
                    avatarInitialText = avatarInitialText,
                    onlineIndicatorEnabled = onlineIndicatorEnabled,
                    onlineIndicatorPosition = onlineIndicatorPosition,
                )
            }
    }
}
