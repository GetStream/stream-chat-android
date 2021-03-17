package io.getstream.chat.android.ui.common.internal.style

import android.content.res.TypedArray
import android.graphics.Typeface
import android.util.TypedValue
import android.widget.TextView

public class TextStyle(private val chatFonts: ChatFonts? = null) {
    private companion object {
        const val UNSET_SIZE = -1
        const val UNSET_COLOR = Integer.MAX_VALUE
        const val UNSET_HINT_COLOR = Integer.MAX_VALUE
        const val UNSET_FONT_RESOURCE = -1
    }

    public var fontResource: Int = UNSET_FONT_RESOURCE
    public var fontAssetsPath: String? = null
    public var style: Int = -1
    public var size: Int = UNSET_SIZE
    public var color: Int = UNSET_COLOR
    public var hintColor: Int = UNSET_HINT_COLOR

    public val font: Typeface?
        get() {
            return chatFonts?.getFont(this)
        }

    public fun apply(textView: TextView) {
        if (size != UNSET_SIZE) {
            textView.setTextSize(TypedValue.COMPLEX_UNIT_PX, size.toFloat())
        }
        if (color != UNSET_COLOR) {
            textView.setTextColor(color)
        }
        if (hintColor != UNSET_HINT_COLOR) {
            textView.setHintTextColor(hintColor)
        }

        chatFonts?.setFont(this, textView)
    }

    public fun hasFont(): Boolean {
        return fontAssetsPath != null || fontResource != UNSET_FONT_RESOURCE
    }

    public class Builder(private val array: TypedArray) {
        private val result: TextStyle = TextStyle()

        public fun size(ref: Int): Builder = size(ref, -1)

        public fun size(ref: Int, defValue: Int): Builder = apply {
            result.size = array.getDimensionPixelSize(ref, defValue)
        }

        public fun font(assetsPath: Int, resId: Int): Builder = apply {
            result.fontAssetsPath = array.getString(assetsPath)
            result.fontResource = array.getResourceId(resId, -1)
        }

        public fun color(ref: Int, defValue: Int): Builder = apply {
            result.color = array.getColor(ref, defValue)
        }

        public fun hintColor(ref: Int, defValue: Int): Builder = apply {
            result.hintColor = array.getColor(ref, defValue)
        }

        public fun style(ref: Int, defValue: Int): Builder = apply {
            result.style = array.getInt(ref, defValue)
        }

        public fun build(): TextStyle {
            return result
        }
    }
}
