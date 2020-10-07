package com.getstream.sdk.chat.style

import android.content.res.TypedArray
import android.graphics.Typeface
import android.util.TypedValue
import android.widget.TextView
import com.getstream.sdk.chat.Chat

class TextStyle {
    private companion object {
        const val UNSET_SIZE = -1
        const val UNSET_COLOR = -1
        const val UNSET_HINT_COLOR = -1
        const val UNSET_FONT_RESOURCE = -1
    }

    var fontResource: Int = UNSET_FONT_RESOURCE
    var fontAssetsPath: String? = null
    var style: Int = -1
    var size: Int = UNSET_SIZE
    var color: Int = UNSET_COLOR
    var hintColor: Int = UNSET_HINT_COLOR

    val font: Typeface?
        get() {
            return Chat.getInstance().fonts.getFont(this)
        }

    fun apply(textView: TextView) {
        val chatFonts = Chat.getInstance().fonts

        if (size != UNSET_SIZE) {
            textView.setTextSize(TypedValue.COMPLEX_UNIT_PX, size.toFloat())
        }
        if (color != UNSET_COLOR) {
            textView.setTextColor(color)
        }
        if (hintColor != UNSET_HINT_COLOR) {
            textView.setHintTextColor(hintColor)
        }

        chatFonts.setFont(this, textView)
    }

    fun hasFont(): Boolean {
        return fontAssetsPath != null || fontResource != UNSET_FONT_RESOURCE
    }

    class Builder(private val array: TypedArray) {
        private val result: TextStyle = TextStyle()

        fun size(ref: Int): Builder = size(ref, -1)

        fun size(ref: Int, defValue: Int): Builder = apply {
            result.size = array.getDimensionPixelSize(ref, defValue)
        }

        fun font(assetsPath: Int, resId: Int): Builder = apply {
            result.fontAssetsPath = array.getString(assetsPath)
            result.fontResource = array.getResourceId(resId, -1)
        }

        fun color(ref: Int, defValue: Int): Builder = apply {
            result.color = array.getColor(ref, defValue)
        }

        fun hintColor(ref: Int, defValue: Int): Builder = apply {
            result.hintColor = array.getColor(ref, defValue)
        }

        fun style(ref: Int, defValue: Int): Builder = apply {
            result.style = array.getInt(ref, defValue)
        }

        fun build(): TextStyle {
            return result
        }
    }
}
