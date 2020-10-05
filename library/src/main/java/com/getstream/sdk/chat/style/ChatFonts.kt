package com.getstream.sdk.chat.style;

import android.graphics.Typeface;
import android.widget.TextView;

import com.getstream.sdk.chat.utils.roundedImageView.CircularImageView;

public interface ChatFonts {
    void setFont(TextStyle fontStyle, TextView textView);
    void setFont(TextStyle fontStyle, CircularImageView imageView, float factor);
    Typeface getFont(TextStyle textStyle);
}
