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
    internal class Builder(private val a: TypedArray, c: Context) {
        private val res = c.resources

        private var avatarWidth: Int = 0
        private var avatarHeight: Int = 0
        private var avatarBorderWidth: Int = 0
        private var avatarBorderColor: Int = 0
        private var avatarBackgroundColor: Int = 0
        private var avatarInitialText: TextStyle = TextStyle()

        fun avatarWidth(
            @StyleableRes avatarWidthStyleableId: Int,
            @DimenRes defaultValue: Int
        ) = apply {
            avatarWidth = a.getDimensionPixelSize(
                avatarWidthStyleableId,
                res.getDimensionPixelSize(defaultValue)
            )
        }

        fun avatarHeight(
            @StyleableRes avatarHeightStyleableId: Int,
            @DimenRes defaultValue: Int
        ) = apply {
            avatarHeight = a.getDimensionPixelSize(
                avatarHeightStyleableId,
                res.getDimensionPixelSize(defaultValue)
            )
        }

        fun avatarBorderWidth(
            @StyleableRes avatarBorderWidthStyleableId: Int,
            @DimenRes defaultValue: Int
        ) = apply {
            avatarBorderWidth = a.getDimensionPixelSize(
                avatarBorderWidthStyleableId,
                res.getDimensionPixelSize(defaultValue)
            )
        }

        fun avatarBorderColor(
            @StyleableRes avatarBorderColorStyleableId: Int,
            @ColorInt defaultColor: Int
        ) = apply {
            avatarBorderColor = a.getColor(avatarBorderColorStyleableId, defaultColor)
        }

        fun avatarBackgroundColor(
            @StyleableRes avatarBackgroundColorStyleableId: Int,
            @ColorInt defaultColor: Int
        ) = apply {
            avatarBackgroundColor = a.getColor(avatarBackgroundColorStyleableId, defaultColor)
        }

        fun avatarInitialText(
            @StyleableRes avatarTextSizeStyleableId: Int,
            @DimenRes avatarTextSizeDefaultValue: Int,
            @StyleableRes avatarTextColorStyleableId: Int,
            @ColorInt avatarTextColorDefaultValue: Int,
            @StyleableRes avatarTextFontAssetsStyleableId: Int = R.styleable.MessageListView_streamAvatarTextFontAssets,
            @StyleableRes avatarTextFontStyleableId: Int = R.styleable.MessageListView_streamAvatarTextFont,
            @StyleableRes avatarTextStyleStyleableId: Int,
            defaultTextStyle: Int = Typeface.BOLD,
        ) = apply {
            avatarInitialText = TextStyle.Builder(a)
                .size(
                    avatarTextSizeStyleableId,
                    res.getDimensionPixelSize(avatarTextSizeDefaultValue)
                )
                .color(avatarTextColorStyleableId, avatarTextColorDefaultValue)
                .font(avatarTextFontAssetsStyleableId, avatarTextFontStyleableId)
                .style(avatarTextStyleStyleableId, defaultTextStyle)
                .build()
        }

        fun build(): AvatarStyle =
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
