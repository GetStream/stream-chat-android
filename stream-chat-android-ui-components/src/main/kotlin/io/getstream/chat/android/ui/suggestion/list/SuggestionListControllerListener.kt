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

/**
 * The default implementation of [SuggestionListControllerListener].
 * Used to change the enabled state of the attachments button.
 *
 * @param onInputStateChanged Used to react to the input state change to enable or disable attachments.
 */
internal class DefaultSuggestionListControllerListener(private val onInputStateChanged: (shouldEnableAttachments: Boolean) -> Unit) :
    SuggestionListControllerListener {

    /**
     * Shows if the suggestion list popup is visible.
     */
    private var isSuggestionListUiVisible: Boolean = false
        set(value) {
            field = value
            onInputStateChanged(!isSuggestionListUiVisible && !inputContainsCommands)
        }

    /**
     * Shows if the input text contains commands.
     */
    private var inputContainsCommands: Boolean = false
        set(value) {
            field = value
            onInputStateChanged(!isSuggestionListUiVisible && !inputContainsCommands)
        }

    /**
     * Called when [SuggestionListUi] changes visibility.
     */
    override fun onSuggestionListUiVisibilityChanged(isVisible: Boolean) {
        isSuggestionListUiVisible = isVisible
    }

    /**
     * Evaluates the input to see if it contains commands.
     */
    override fun containsCommands(doesContain: Boolean) {
        inputContainsCommands = doesContain
    }

    /**
     * Evaluates the input to see if it contains mentions.
     */
    override fun containsMentions(doesContain: Boolean) {}
}
