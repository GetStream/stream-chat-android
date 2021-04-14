package io.getstream.chat.android.ui.suggestion.internal

import io.getstream.chat.android.ui.suggestion.Suggestions

internal interface SuggestionListUi {
    fun renderSuggestions(suggestions: Suggestions)
    fun isSuggestionListVisible(): Boolean
}
