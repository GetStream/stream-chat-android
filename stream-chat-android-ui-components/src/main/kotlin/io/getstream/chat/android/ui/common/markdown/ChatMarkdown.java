package io.getstream.chat.android.ui.common.markdown;

import android.widget.TextView;

import androidx.annotation.NonNull;

public interface ChatMarkdown {
    void setText(@NonNull TextView textView, @NonNull String text);
}
