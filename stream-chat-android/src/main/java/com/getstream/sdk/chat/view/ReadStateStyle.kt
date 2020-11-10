package com.getstream.sdk.chat.view

import android.content.Context
import android.content.res.TypedArray
import android.graphics.Typeface
import androidx.annotation.ColorInt
import androidx.annotation.DimenRes
import androidx.annotation.StyleableRes
import com.getstream.sdk.chat.style.TextStyle

public data class ReadStateStyle(
    public val readStateText: TextStyle = TextStyle(),
    public val isReadStateEnabled: Boolean = false,
    public val isDeliveredIndicatorEnabled: Boolean = false,
    public val readStateAvatarWidth: Int = 0,
    public val readStateAvatarHeight: Int = 0,
) {
    internal class Builder(private val a: TypedArray, c: Context) {
        private val res = c.resources

        private var readStateText: TextStyle = TextStyle()
        private var isReadStateEnabled: Boolean = false
        private var isDeliveredIndicatorEnabled: Boolean = false
        private var readStateAvatarWidth: Int = 0
        private var readStateAvatarHeight: Int = 0

        fun readStateText(
            @StyleableRes textSize: Int,
            @DimenRes defaultTextSize: Int,
            @StyleableRes textColor: Int,
            @ColorInt defaultTextColor: Int,
            @StyleableRes textFontAssetsStyleableId: Int,
            @StyleableRes textFontStyleableId: Int,
            @StyleableRes textStyleStyleableId: Int,
            textStyleDefault: Int = Typeface.BOLD
        ): Builder {
            readStateText = TextStyle.Builder(a)
                .size(textSize, res.getDimensionPixelSize(defaultTextSize))
                .color(textColor, defaultTextColor)
                .font(textFontAssetsStyleableId, textFontStyleableId)
                .style(textStyleStyleableId, textStyleDefault)
                .build()
            return this
        }

        fun isReadStateEnabled(
            @StyleableRes isReadStateEnabled: Int,
            defaultValue: Boolean
        ): Builder {
            this.isReadStateEnabled = a.getBoolean(isReadStateEnabled, defaultValue)
            return this
        }

        fun isDeliveredIndicatorEnabled(
            @StyleableRes isDeliveredIndicatorEnabled: Int,
            defaultValue: Boolean
        ): Builder {
            this.isDeliveredIndicatorEnabled =
                a.getBoolean(isDeliveredIndicatorEnabled, defaultValue)
            return this
        }

        fun readStateAvatarWidth(
            @StyleableRes readStateAvatarWidth: Int,
            defaultValue: Int
        ): Builder {
            this.readStateAvatarWidth = a.getDimensionPixelSize(readStateAvatarWidth, defaultValue)
            return this
        }

        fun readStateAvatarHeight(
            @StyleableRes readStateAvatarHeight: Int,
            defaultValue: Int
        ): Builder {
            this.readStateAvatarHeight =
                a.getDimensionPixelSize(readStateAvatarHeight, defaultValue)
            return this
        }

        fun build(): ReadStateStyle = ReadStateStyle(
            readStateText,
            isReadStateEnabled,
            isDeliveredIndicatorEnabled,
            readStateAvatarWidth,
            readStateAvatarHeight
        )
    }
}
