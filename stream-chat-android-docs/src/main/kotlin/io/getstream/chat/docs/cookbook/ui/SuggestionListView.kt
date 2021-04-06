package io.getstream.chat.docs.cookbook.ui

import io.getstream.chat.android.ui.message.input.MessageInputView
import io.getstream.chat.android.ui.suggestion.list.SuggestionListView

/**
 * @see <a href="https://github.com/GetStream/stream-chat-android/wiki/UI-Cookbook#suggestion-list-view">Suggestion List View</a>
 */
class SuggestionListView {
    lateinit var messageInputView: MessageInputView
    lateinit var suggestionListView: SuggestionListView

    fun connectingSuggestionListViewWithMessageInputView() {
        messageInputView.setSuggestionListView(suggestionListView)
    }
}
