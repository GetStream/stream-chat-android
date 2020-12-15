package com.getstream.sdk.chat.style

import android.graphics.Typeface
import android.widget.TextView

public interface ChatFonts {
    public fun setFont(textStyle: TextStyle, textView: TextView)
    public fun getFont(textStyle: TextStyle): Typeface?
}
