package io.getstream.chat.android.ui.suggestion.list

import io.getstream.chat.android.core.ExperimentalStreamChatApi
import io.getstream.chat.android.ui.suggestion.Suggestions

@ExperimentalStreamChatApi
public interface SuggestionListUi {
    public fun renderSuggestions(suggestions: Suggestions)
    public fun isSuggestionListVisible(): Boolean
}
