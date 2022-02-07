package io.getstream.chat.android.ui.common.markdown

import android.widget.TextView
import kotlin.Deprecated

@Deprecated(
    message = "ChatMarkdown is deprecated in favour of ChatMessageTextTransformer. " +
        "Use ChatMessageTextTransformer for text ui transformations."
)
public fun interface ChatMarkdown {
    public fun setText(textView: TextView, text: String)
}
