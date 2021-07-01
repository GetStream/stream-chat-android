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

@ExperimentalStreamChatApi
public class SuggestionListController(
    private val suggestionListUi: SuggestionListUi,
) {
    private val scope = CoroutineScope(DispatcherProvider.Main)
    private var currentSuggestions: Suggestions by Delegates.observable(Suggestions.EmptySuggestions) { _, _, newSuggestions ->
        scope.launch { renderSuggestions(newSuggestions) }
    }
    public var userLookupHandler: MessageInputView.UserLookupHandler
        by Delegates.observable(MessageInputView.DefaultUserLookupHandler(emptyList())) { _, _, _ -> computeSuggestions() }
    public var commands: List<Command> by Delegates.observable(emptyList()) { _, _, _ -> computeSuggestions() }
    public var mentionsEnabled: Boolean by Delegates.observable(true) { _, _, _ -> computeSuggestions() }
    public var commandsEnabled: Boolean by Delegates.observable(true) { _, _, _ -> computeSuggestions() }
    private var messageText: String by Delegates.observable(String.EMPTY) { _, _, _ -> computeSuggestions() }

    public fun onNewMessageText(newMessageText: String) {
        messageText = newMessageText
    }

    private fun computeSuggestions() {
        scope.launch {
            when {
                commandsEnabled && messageText.isCommandMessage() -> {
                    currentSuggestions = messageText.getCommandSuggestions()
                }
                mentionsEnabled && messageText.isMentionMessage() -> {
                    handleUserLookup(userLookupHandler, messageText)
                }
                else -> hideSuggestionList()
            }
        }
    }

    private suspend fun handleUserLookup(
        userLookupHandler: MessageInputView.UserLookupHandler,
        messageText: String,
    ) {
        currentSuggestions = withContext(DispatcherProvider.IO) {
            Suggestions.MentionSuggestions(userLookupHandler.handleUserLookup(messageText.substringAfterLast("@")))
        }
    }

    public fun showAvailableCommands() {
        currentSuggestions = Suggestions.CommandSuggestions(commands).takeIf { commandsEnabled } ?: Suggestions.EmptySuggestions
    }

    public fun hideSuggestionList() {
        currentSuggestions = Suggestions.EmptySuggestions
    }

    public fun isSuggestionListVisible(): Boolean {
        return suggestionListUi.isSuggestionListVisible()
    }

    private suspend fun renderSuggestions(suggestions: Suggestions) = withContext(DispatcherProvider.Main) {
        suggestionListUi.renderSuggestions(suggestions)
    }

    private fun String.isCommandMessage() = COMMAND_PATTERN.matcher(this).find()

    private fun String.isMentionMessage() = MENTION_PATTERN.matcher(this).find()

    private fun String.getCommandSuggestions(): Suggestions.CommandSuggestions {
        val commandPattern = removePrefix("/")
        return commands
            .filter { it.name.startsWith(commandPattern) }
            .let { Suggestions.CommandSuggestions(it) }
    }

    private companion object {
        private val COMMAND_PATTERN = Pattern.compile("^/[a-z]*$")
        private val MENTION_PATTERN = Pattern.compile("^(.* )?@([a-zA-Z]+[0-9]*)*$")
    }
}
