package io.getstream.chat.android.ui.common.style

import android.graphics.Typeface
import android.widget.TextView

public interface ChatFonts {
    public fun setFont(textStyle: TextStyle, textView: TextView)
    public fun setFont(textStyle: TextStyle, textView: TextView, defaultTypeface: Typeface = Typeface.DEFAULT)
    public fun getFont(textStyle: TextStyle): Typeface?
}
