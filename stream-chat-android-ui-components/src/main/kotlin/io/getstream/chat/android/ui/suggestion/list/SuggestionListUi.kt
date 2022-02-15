package io.getstream.chat.android.ui.suggestion.list

import io.getstream.chat.android.core.ExperimentalStreamChatApi
import io.getstream.chat.android.ui.suggestion.Suggestions

@ExperimentalStreamChatApi
/**
 * Interface used for communication with the suggestion list view.
 */
public interface SuggestionListUi {

    /**
     * Renders the suggestion list View.
     */
    public fun renderSuggestions(suggestions: Suggestions)

    /**
     * Shows if the suggestion list is currently visible.
     */
    public fun isSuggestionListVisible(): Boolean
}
