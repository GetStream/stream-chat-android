package com.getstream.sdk.chat.style

import android.content.res.TypedArray
import android.graphics.drawable.Drawable
import android.widget.TextView
import androidx.annotation.Px
import androidx.annotation.StyleableRes
import io.getstream.chat.android.core.internal.InternalStreamChatApi

@InternalStreamChatApi
public class ScrollbarStyle {
    private companion object {
        private const val UNSET_SCROLLBAR_WIDTH = -1
    }

    private var inputScrollbarEnabled: Boolean = false
    private var inputScrollbarFadingEnabled: Boolean = false
    private var inputScrollbarWidth: Int = UNSET_SCROLLBAR_WIDTH
    private var inputScrollbarThumb: Drawable? = null

    public fun apply(textView: TextView) {
        textView.isVerticalScrollBarEnabled = inputScrollbarEnabled

        if (inputScrollbarEnabled) {
            textView.isScrollbarFadingEnabled = inputScrollbarFadingEnabled

            if (inputScrollbarWidth != UNSET_SCROLLBAR_WIDTH) {
                textView.scrollBarSize = inputScrollbarWidth
            }

            inputScrollbarThumb?.let {
                textView.verticalScrollbarThumbDrawable = it
            }
        }
    }

    public class Builder(private val array: TypedArray) {
        private val style: ScrollbarStyle = ScrollbarStyle()

        public fun scrollbarEnabled(@StyleableRes res: Int, defValue: Boolean = false): Builder = apply {
            style.inputScrollbarEnabled = array.getBoolean(res, defValue)
        }

        public fun scrollbarFadingEnabled(@StyleableRes res: Int, defValue: Boolean = false): Builder = apply {
            style.inputScrollbarFadingEnabled = array.getBoolean(res, defValue)
        }

        public fun scrollbarWidth(@StyleableRes res: Int, @Px defValue: Int = UNSET_SCROLLBAR_WIDTH): Builder = apply {
            style.inputScrollbarWidth = array.getDimensionPixelSize(res, defValue)
        }

        public fun scrollbarThumb(@StyleableRes res: Int): Builder = apply {
            style.inputScrollbarThumb = array.getDrawable(res)
        }

        public fun build(): ScrollbarStyle = style
    }
}
