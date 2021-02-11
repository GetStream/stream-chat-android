package io.getstream.chat.android.ui.suggestion.internal

import io.getstream.chat.android.client.models.Command
import io.getstream.chat.android.client.models.User
import io.getstream.chat.android.client.models.name
import io.getstream.chat.android.ui.common.extensions.internal.EMPTY
import io.getstream.chat.android.ui.suggestion.list.SuggestionListView
import java.util.regex.Pattern

internal class SuggestionListController(
    private val suggestionListView: SuggestionListView,
    private val dismissListener: SuggestionListDismissListener
) {
    var users: List<User> = emptyList()
        set(value) {
            field = value
            showSuggestions(messageText)
        }
    var commands: List<Command> = emptyList()
        set(value) {
            field = value
            showSuggestions(messageText)
        }
    private var messageText: String = String.EMPTY

    fun showSuggestions(messageText: String) {
        this.messageText = messageText
        when {
            messageText.isCommandMessage() -> suggestionListView.showSuggestionList(messageText.getCommandSuggestions())
            messageText.isMentionMessage() -> suggestionListView.showSuggestionList(messageText.getMentionSuggestions())
            else -> hideSuggestionList()
        }
    }

    fun showAvailableCommands() {
        suggestionListView.showSuggestionList(SuggestionListView.Suggestions.CommandSuggestions(commands))
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

    private fun String.getMentionSuggestions(): SuggestionListView.Suggestions.MentionSuggestions {
        val namePattern = substringAfterLast("@")
        return users
            .filter { it.name.contains(namePattern, true) }
            .let { SuggestionListView.Suggestions.MentionSuggestions(it) }
    }

    internal fun interface SuggestionListDismissListener {
        fun onDismissed()
    }

    companion object {
        private val COMMAND_PATTERN = Pattern.compile("^/[a-z]*$")
        private val MENTION_PATTERN = Pattern.compile("^(.* )?@([a-zA-Z]+[0-9]*)*$")
    }
}
