package io.getstream.chat.android.ui.suggestion.internal

import io.getstream.chat.android.ui.suggestion.Suggestions

internal interface SuggestionListUi {

    fun showSuggestionList(suggestions: Suggestions)

    fun hideSuggestionList()

    fun isSuggestionListVisible(): Boolean
}
