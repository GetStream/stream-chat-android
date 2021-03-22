package io.getstream.chat.android.ui.common.style

import android.graphics.Typeface
import android.widget.TextView
import com.getstream.sdk.chat.style.TextStyle

public interface ChatFonts {
    public fun setFont(textStyle: TextStyle, textView: TextView)
    public fun getFont(textStyle: TextStyle): Typeface?
}
