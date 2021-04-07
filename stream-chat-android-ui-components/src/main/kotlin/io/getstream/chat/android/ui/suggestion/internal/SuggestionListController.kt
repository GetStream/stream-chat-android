package io.getstream.chat.android.ui.suggestion.internal

import io.getstream.chat.android.client.models.Command
import io.getstream.chat.android.core.internal.coroutines.DispatcherProvider
import io.getstream.chat.android.ui.common.extensions.internal.EMPTY
import io.getstream.chat.android.ui.message.input.MessageInputView
import io.getstream.chat.android.ui.suggestion.list.SuggestionListView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.regex.Pattern

internal class SuggestionListController(
    private val suggestionListView: SuggestionListView,
    private val dismissListener: SuggestionListDismissListener,
) {
    var commands: List<Command> = emptyList()
        set(value) {
            field = value
            showSuggestions(messageText)
        }
    var mentionsEnabled: Boolean = true
    var commandsEnabled: Boolean = true

    private var messageText: String = String.EMPTY

    fun showSuggestions(
        messageText: String,
        onUserLookupListener: MessageInputView.OnUserLookupListener? = null,
        scope: CoroutineScope? = null,
    ) {
        this.messageText = messageText
        when {
            commandsEnabled && messageText.isCommandMessage() -> {
                suggestionListView.showSuggestionList(messageText.getCommandSuggestions())
            }
            mentionsEnabled && messageText.isMentionMessage() -> {
                handleUserLookup(scope, onUserLookupListener, messageText)
            }
            else -> hideSuggestionList()
        }
    }

    private fun handleUserLookup(
        scope: CoroutineScope?,
        onUserLookupListener: MessageInputView.OnUserLookupListener?,
        messageText: String,
    ) {
        scope?.launch {
            val suggestions = withContext(DispatcherProvider.IO) {
                onUserLookupListener?.onUserLookup(messageText)
                    ?.let(SuggestionListView.Suggestions::MentionSuggestions)
            }
            suggestions?.let(suggestionListView::showSuggestionList)
        }
    }

    fun showAvailableCommands() {
        if (commandsEnabled) {
            suggestionListView.showSuggestionList(SuggestionListView.Suggestions.CommandSuggestions(commands))
        }
    }

    fun hideSuggestionList() {
        suggestionListView.hideSuggestionList()
        dismissListener.onDismissed()
    }

    fun isSuggestionListVisible(): Boolean {
        return suggestionListView.isSuggestionListVisible()
    }

    private fun String.isCommandMessage() = COMMAND_PATTERN.matcher(this).find()

    private fun String.isMentionMessage() = MENTION_PATTERN.matcher(this).find()

    private fun String.getCommandSuggestions(): SuggestionListView.Suggestions.CommandSuggestions {
        val commandPattern = removePrefix("/")
        return commands
            .filter { it.name.startsWith(commandPattern) }
            .let { SuggestionListView.Suggestions.CommandSuggestions(it) }
    }

    internal fun interface SuggestionListDismissListener {
        fun onDismissed()
    }

    companion object {
        private val COMMAND_PATTERN = Pattern.compile("^/[a-z]*$")
        private val MENTION_PATTERN = Pattern.compile("^(.* )?@([a-zA-Z]+[0-9]*)*$")
    }
}
