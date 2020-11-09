package com.getstream.sdk.chat.view.messages

import android.content.Context
import android.content.res.TypedArray
import android.graphics.Typeface
import androidx.annotation.ColorInt
import androidx.annotation.DimenRes
import androidx.annotation.StyleableRes
import com.getstream.sdk.chat.R
import com.getstream.sdk.chat.style.TextStyle

public data class AvatarStyle(
    public val avatarWidth: Int = 0,
    public val avatarHeight: Int = 0,
    public val avatarBorderWidth: Int = 0,
    public val avatarBorderColor: Int = 0,
    public val avatarBackgroundColor: Int = 0,
    public val avatarInitialText: TextStyle = TextStyle()
) {
    public class Builder(private val a: TypedArray, private val c: Context) {
        private val res = c.resources

        private var avatarWidth: Int = 0
        private var avatarHeight: Int = 0
        private var avatarBorderWidth: Int = 0
        private var avatarBorderColor: Int = 0
        private var avatarBackgroundColor: Int = 0
        private var avatarInitialText: TextStyle = TextStyle()

        public fun avatarWidth(
            @StyleableRes avatarWidthStyleableId: Int,
            @DimenRes defaultValue: Int
        ): Builder {
            avatarWidth = a.getDimensionPixelSize(
                avatarWidthStyleableId,
                res.getDimensionPixelSize(defaultValue)
            )
            return this
        }

        public fun avatarHeight(
            @StyleableRes avatarHeightStyleableId: Int,
            @DimenRes defaultValue: Int
        ): Builder {
            avatarHeight = a.getDimensionPixelSize(
                avatarHeightStyleableId, res.getDimensionPixelSize(defaultValue)
            )
            return this
        }

        public fun avatarBorderWidth(
            @StyleableRes avatarBorderWidthStyleableId: Int,
            @DimenRes defaultValue: Int
        ): Builder {
            avatarBorderWidth = a.getDimensionPixelSize(
                avatarBorderWidthStyleableId,
                res.getDimensionPixelSize(defaultValue)
            )
            return this
        }

        public fun avatarBorderColor(
            @StyleableRes avatarBorderColorStyleableId: Int,
            @ColorInt defaultColor: Int
        ): Builder {
            avatarBorderColor = a.getColor(avatarBorderColorStyleableId, defaultColor)
            return this
        }

        public fun avatarBackgroundColor(
            @StyleableRes avatarBackgroundColorStyleableId: Int,
            @ColorInt defaultColor: Int
        ): Builder {
            avatarBackgroundColor = a.getColor(avatarBackgroundColorStyleableId, defaultColor)
            return this
        }

        public fun avatarInitialText(
            @StyleableRes avatarTextSizeStyleableId: Int,
            @DimenRes avatarTextSizeDefaultValue: Int,
            @StyleableRes avatarTextColorStyleableId: Int,
            @ColorInt avatarTextColorDefaultValue: Int,
            @StyleableRes avatarTextFontAssetsStyleableId: Int = R.styleable.MessageListView_streamAvatarTextFontAssets,
            @StyleableRes avatarTextFontStyleableId: Int = R.styleable.MessageListView_streamAvatarTextFont,
            @StyleableRes avatarTextStyleStyleableId: Int,
            defaultTextStyle: Int = Typeface.BOLD,
        ): Builder {
            avatarInitialText = TextStyle.Builder(a)
                .size(
                    avatarTextSizeStyleableId,
                    res.getDimensionPixelSize(avatarTextSizeDefaultValue)
                )
                .color(avatarTextColorStyleableId, avatarTextColorDefaultValue)
                .font(avatarTextFontAssetsStyleableId, avatarTextFontStyleableId)
                .style(avatarTextStyleStyleableId, defaultTextStyle)
                .build()
            return this
        }

        public fun build(): AvatarStyle =
            AvatarStyle(
                avatarWidth,
                avatarHeight,
                avatarBorderWidth,
                avatarBorderColor,
                avatarBackgroundColor,
                avatarInitialText
            )
    }
}
