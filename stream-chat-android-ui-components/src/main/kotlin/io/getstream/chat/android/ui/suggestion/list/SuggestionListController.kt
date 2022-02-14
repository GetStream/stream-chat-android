package io.getstream.chat.android.ui.suggestion.list

import io.getstream.chat.android.client.models.Command
import io.getstream.chat.android.core.ExperimentalStreamChatApi
import io.getstream.chat.android.core.internal.coroutines.DispatcherProvider
import io.getstream.chat.android.ui.common.extensions.internal.EMPTY
import io.getstream.chat.android.ui.message.input.MessageInputView
import io.getstream.chat.android.ui.suggestion.Suggestions
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.regex.Pattern
import kotlin.properties.Delegates

/**
 * Feeds data to and controls the visibility of [SuggestionListUi].
 *
 * @param suggestionListUi Used for communication with the View.
 * @param suggestionListControllerListener Listener used for event handling.
 */
@ExperimentalStreamChatApi
public class SuggestionListController(
    private val suggestionListUi: SuggestionListUi,
    private val suggestionListControllerListener: SuggestionListControllerListener? = null,
) {
    private val scope = CoroutineScope(DispatcherProvider.Main)
    private var currentSuggestions: Suggestions by Delegates.observable(Suggestions.EmptySuggestions) { _, _, newSuggestions ->
        scope.launch { renderSuggestions(newSuggestions) }
    }

    /**
     * Lookup Handler used for searching for users to be mentioned.
     *
     * @see MessageInputView.UserLookupHandler
     * @see MessageInputView.DefaultUserLookupHandler
     */
    public var userLookupHandler: MessageInputView.UserLookupHandler
        by Delegates.observable(MessageInputView.DefaultUserLookupHandler(emptyList())) { _, _, _ -> computeSuggestions() }

    /**
     * The list of available commands.
     *
     * Reactively recomputes suggestions upon setting.
     */
    public var commands: List<Command> by Delegates.observable(emptyList()) { _, _, _ -> computeSuggestions() }

    /**
     * Enables or disables mention suggestions.
     *
     * Reactively recomputes suggestions upon setting.
     */
    public var mentionsEnabled: Boolean by Delegates.observable(true) { _, _, _ -> computeSuggestions() }

    /**
     * Enables or disables commands suggestions.
     *
     * Reactively recomputes suggestions upon setting.
     */
    public var commandsEnabled: Boolean by Delegates.observable(true) { _, _, _ -> computeSuggestions() }

    /**
     * Text used for displaying suggestions based on the content.
     *
     * Reactively recomputes suggestions upon setting.
     */
    private var messageText: String by Delegates.observable(String.EMPTY) { _, _, _ -> computeSuggestions() }

    /**
     * Updates the message text used for displaying suggestions.
     */
    public fun onNewMessageText(newMessageText: String) {
        messageText = newMessageText
    }

    /**
     * Triggers the computation of suggestions if commands or mentions are enabled
     * and the message contains significant patterns.
     *
     * For patterns:
     * @see SuggestionListController.COMMAND_PATTERN
     * @see SuggestionListController.MENTION_PATTERN
     */
    private fun computeSuggestions() {
        scope.launch {
            when {
                commandsEnabled && messageText.isCommandMessage() -> {
                    suggestionListControllerListener?.containsCommands(true)
                    currentSuggestions = messageText.getCommandSuggestions()
                }
                mentionsEnabled && messageText.isMentionMessage() -> {
                    suggestionListControllerListener?.containsMentions(true)
                    handleUserLookup(userLookupHandler, messageText)
                }
                else -> hideSuggestionList()
            }
        }
    }

    /**
     * Handles the user lookup used to generate mention suggestions.
     *
     * @see MessageInputView.UserLookupHandler
     * @see MessageInputView.DefaultUserLookupHandler
     */
    private suspend fun handleUserLookup(
        userLookupHandler: MessageInputView.UserLookupHandler,
        messageText: String,
    ) {
        currentSuggestions = withContext(DispatcherProvider.IO) {
            Suggestions.MentionSuggestions(userLookupHandler.handleUserLookup(messageText.substringAfterLast("@")))
        }
    }

    /**
     * Displays the list of available command suggestions if any exist.
     */
    public fun showAvailableCommands() {
        currentSuggestions =
            Suggestions.CommandSuggestions(commands).takeIf { commandsEnabled } ?: Suggestions.EmptySuggestions
    }

    /**
     * Hides the suggestion list and notifies [suggestionListControllerListener]
     * that the input doesn't contain commands or mentions.
     */
    public fun hideSuggestionList() {
        suggestionListControllerListener?.containsCommands(false)
        suggestionListControllerListener?.containsMentions(false)
        currentSuggestions = Suggestions.EmptySuggestions
    }

    /**
     * Returns the suggestion list visibility.
     */
    public fun isSuggestionListVisible(): Boolean {
        return suggestionListUi.isSuggestionListVisible()
    }

    /**
     * Renders the suggestion list UI.
     */
    private suspend fun renderSuggestions(suggestions: Suggestions) = withContext(DispatcherProvider.Main) {
        suggestionListUi.renderSuggestions(suggestions)
        suggestionListControllerListener?.onSuggestionListUiVisibilityChanged(suggestionListUi.isSuggestionListVisible())
    }

    /**
     * Checks if a string contains command patterns.
     */
    private fun String.isCommandMessage() = COMMAND_PATTERN.matcher(this).find()

    /**
     * Checks if a string contains mention patterns.
     */
    private fun String.isMentionMessage() = MENTION_PATTERN.matcher(this).find()

    /**
     * Uses the receiver string to fetch a list of command suggestions.
     */
    private fun String.getCommandSuggestions(): Suggestions.CommandSuggestions {
        val commandPattern = removePrefix("/")
        return commands
            .filter { it.name.startsWith(commandPattern) }
            .let { Suggestions.CommandSuggestions(it) }
    }

    private companion object {
        /**
         * Pattern used for matching commands.
         */
        private val COMMAND_PATTERN = Pattern.compile("^/[a-z]*$")

        /**
         * Pattern used for matching mentions. Includes [Pattern.MULTILINE] flag for proper newline support.
         */
        private val MENTION_PATTERN = Pattern.compile("^(.* )?@([a-zA-Z]+[0-9]*)*$", Pattern.MULTILINE)
    }
}
