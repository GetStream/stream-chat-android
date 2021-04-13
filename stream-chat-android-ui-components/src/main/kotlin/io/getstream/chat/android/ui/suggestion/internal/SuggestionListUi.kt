package io.getstream.chat.android.ui.suggestion.internal

import io.getstream.chat.android.ui.suggestion.list.SuggestionListView

internal interface SuggestionListUi {

    fun showSuggestionList(suggestions: SuggestionListView.Suggestions)

    fun hideSuggestionList()

    fun isSuggestionListVisible(): Boolean
}
