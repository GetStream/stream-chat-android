package io.getstream.chat.android.ui.suggestion.list

/**
 * A listener meant to be used for [SuggestionListController].
 */
public interface SuggestionListControllerListener {

    /**
     * Called when [SuggestionListUi] changes visibility.
     */
    public fun onSuggestionListUiVisibilityChanged(isVisible: Boolean)

    /**
     * Evaluates the input to see if it contains commands.
     */
    public fun containsCommands(doesContain: Boolean)

    /**
     * Evaluates the input to see if it contains mentions.
     */
    public fun containsMentions(doesContain: Boolean)
}
