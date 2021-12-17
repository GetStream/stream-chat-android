package com.getstream.sdk.chat;

import android.widget.TextView;

import androidx.annotation.NonNull;

import kotlin.Deprecated;

@Deprecated(message = "ChatMarkdown is deprecated in favour of ChatMessageTextTransformer. " +
        "Use ChatMessageTextTransformer for text ui transformations."
)
public interface ChatMarkdown {
    void setText(@NonNull TextView textView, @NonNull String text);
}
