/*
 * Copyright (c) 2014-2026 Stream.io Inc. All rights reserved.
 *
 * Licensed under the Stream License;
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    https://github.com/GetStream/stream-chat-android/blob/main/LICENSE
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.getstream.chat.android.ui.font

import android.content.res.TypedArray
import android.graphics.Typeface
import android.util.TypedValue
import android.widget.TextView
import androidx.annotation.AnyRes
import androidx.annotation.ColorInt
import androidx.annotation.Px
import androidx.annotation.StyleableRes
import io.getstream.chat.android.ui.ChatUI
import java.io.Serializable

public data class TextStyle(
    @AnyRes public val fontResource: Int = UNSET_FONT_RESOURCE,
    public val fontAssetsPath: String? = null,
    public val style: Int = Typeface.NORMAL,
    @Px public val size: Int = UNSET_SIZE,
    @ColorInt public val color: Int = UNSET_COLOR,
    public val hint: String = UNSET_HINT,
    @ColorInt public val hintColor: Int = UNSET_HINT_COLOR,
    public val defaultFont: Typeface = Typeface.DEFAULT,
) : Serializable {
    private companion object {
        const val UNSET_SIZE = -1
        const val UNSET_COLOR = Integer.MAX_VALUE
        const val UNSET_HINT_COLOR = Integer.MAX_VALUE
        const val UNSET_FONT_RESOURCE = -1
        const val UNSET_HINT = ""
    }

    public val font: Typeface?
        get() {
            return ChatUI.fonts.getFont(this)
        }

    public fun apply(textView: TextView) {
        val chatFonts = ChatUI.fonts

        if (size != UNSET_SIZE) {
            textView.setTextSize(TypedValue.COMPLEX_UNIT_PX, size.toFloat())
        }
        if (color != UNSET_COLOR) {
            textView.setTextColor(color)
        }
        if (hint != UNSET_HINT) {
            textView.hint = hint
        }
        if (hintColor != UNSET_HINT_COLOR) {
            textView.setHintTextColor(hintColor)
        }

        chatFonts.setFont(this, textView, defaultFont)
    }

    public fun hasFont(): Boolean {
        return fontAssetsPath != null || fontResource != UNSET_FONT_RESOURCE
    }

    public fun colorOrNull(): Int? = if (color != UNSET_COLOR) color else null

    public class Builder(private val array: TypedArray) {
        private var fontResource: Int = UNSET_FONT_RESOURCE
        private var fontAssetsPath: String? = null
        private var style: Int = Typeface.NORMAL
        private var size: Int = UNSET_SIZE
        private var color: Int = UNSET_COLOR
        private var hint: String = UNSET_HINT
        private var hintColor: Int = UNSET_HINT_COLOR
        private var defaultFont: Typeface = Typeface.DEFAULT

        public fun size(@StyleableRes ref: Int): Builder = size(ref, -1)

        public fun size(@StyleableRes ref: Int, @Px defValue: Int): Builder = apply {
            this.size = array.getDimensionPixelSize(ref, defValue)
        }

        public fun font(@StyleableRes assetsPath: Int, @StyleableRes resId: Int): Builder = apply {
            this.fontAssetsPath = array.getString(assetsPath)
            this.fontResource = array.getResourceId(resId, -1)
        }

        public fun fontAssetsPath(@StyleableRes assetsPath: Int, defValue: String?): Builder = apply {
            this.fontAssetsPath = array.getString(assetsPath) ?: defValue
        }

        public fun fontResource(@StyleableRes resId: Int, defValue: Int): Builder = apply {
            this.fontResource = array.getResourceId(resId, defValue)
        }

        public fun font(
            @StyleableRes assetsPath: Int,
            @StyleableRes resId: Int,
            defaultFont: Typeface,
        ): Builder = apply {
            this.fontAssetsPath = array.getString(assetsPath)
            this.fontResource = array.getResourceId(resId, -1)
            this.defaultFont = defaultFont
        }

        public fun color(@StyleableRes ref: Int, @ColorInt defValue: Int): Builder = apply {
            this.color = array.getColor(ref, defValue)
        }

        public fun hintColor(@StyleableRes ref: Int, @ColorInt defValue: Int): Builder = apply {
            this.hintColor = array.getColor(ref, defValue)
        }

        public fun hint(@StyleableRes ref: Int, defValue: String): Builder = apply {
            this.hint = array.getString(ref) ?: defValue
        }

        public fun style(@StyleableRes ref: Int, defValue: Int): Builder = apply {
            this.style = array.getInt(ref, defValue)
        }

        public fun build(): TextStyle {
            return TextStyle(
                fontResource = fontResource,
                fontAssetsPath = fontAssetsPath,
                style = style,
                size = size,
                color = color,
                hint = hint,
                hintColor = hintColor,
                defaultFont = defaultFont,
            )
        }
    }
}

internal fun TextView.setTextStyle(textStyle: TextStyle) {
    textStyle.apply(this)
}
