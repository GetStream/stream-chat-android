package io.getstream.chat.android.ui.suggestion.internal

import io.getstream.chat.android.client.models.Command
import io.getstream.chat.android.core.internal.coroutines.DispatcherProvider
import io.getstream.chat.android.ui.common.extensions.internal.EMPTY
import io.getstream.chat.android.ui.message.input.MessageInputView
import io.getstream.chat.android.ui.suggestion.Suggestions
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import java.util.regex.Pattern
import kotlin.properties.Delegates

internal class SuggestionListController(
    private val suggestionListUi: SuggestionListUi,
) {
    private val scope = CoroutineScope(DispatcherProvider.Main)
    private var currentSuggestions: Suggestions by Delegates.observable(Suggestions.EmptySuggestions) { _, _, newSuggestions ->
        scope.launch { renderSuggestions(newSuggestions) }
    }

    var commands: List<Command> = emptyList()
        set(value) {
            field = value
            runBlocking { showSuggestions(messageText) }
        }
    var mentionsEnabled: Boolean = true
    var commandsEnabled: Boolean = true

    private var messageText: String = String.EMPTY

    suspend fun showSuggestions(
        messageText: String,
        userLookupHandler: MessageInputView.UserLookupHandler? = null,
    ) {
        this.messageText = messageText
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

    private suspend fun handleUserLookup(
        userLookupHandler: MessageInputView.UserLookupHandler?,
        messageText: String,
    ) {
        currentSuggestions = withContext(DispatcherProvider.IO) {
            userLookupHandler?.handleUserLookup(messageText.substringAfterLast("@"))
                ?.let(Suggestions::MentionSuggestions)
                ?: Suggestions.EmptySuggestions
        }
    }

    fun showAvailableCommands() {
        currentSuggestions = Suggestions.CommandSuggestions(commands).takeIf { commandsEnabled } ?: Suggestions.EmptySuggestions
    }

    fun hideSuggestionList() {
        currentSuggestions = Suggestions.EmptySuggestions
    }

    fun isSuggestionListVisible(): Boolean {
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

    companion object {
        private val COMMAND_PATTERN = Pattern.compile("^/[a-z]*$")
        private val MENTION_PATTERN = Pattern.compile("^(.* )?@([a-zA-Z]+[0-9]*)*$")
    }
}
