package com.getstream.sdk.chat.style

import android.graphics.Typeface
import android.widget.TextView

interface ChatFonts {
    fun setFont(textStyle: TextStyle, textView: TextView)
    fun getFont(textStyle: TextStyle): Typeface?
}
