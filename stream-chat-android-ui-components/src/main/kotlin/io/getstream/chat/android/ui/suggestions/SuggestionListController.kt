package io.getstream.chat.android.ui.suggestions

import android.widget.EditText
import io.getstream.chat.android.client.models.Command
import io.getstream.chat.android.client.models.User
import io.getstream.chat.android.client.models.name
import java.util.regex.Pattern

internal class SuggestionListController(
    private val suggestionListView: SuggestionListView,
    private val messageTextInput: EditText
) {
    var users: List<User> = emptyList()
    var commands: List<Command> = emptyList()

    private var messageText: String
        get() = messageTextInput.text.toString()
        set(text) {
            messageTextInput.requestFocus()
            messageTextInput.setText(text)
            messageTextInput.setSelection(messageTextInput.text.length)
        }

    init {
        suggestionListView.setOnSuggestionClickListener(
            object : SuggestionListView.OnSuggestionClickListener {
                override fun onMentionClick(user: User) {
                    messageText = "${messageText.substringBeforeLast("@")}@${user.name} "
                }

                override fun onCommandClick(command: Command) {
                    messageText = "/${command.name} "
                }
            }
        )
    }

    fun onTextChanged(inputText: String) {
        when {
            inputText.isCommandMessage() -> suggestionListView.setSuggestions(inputText.getCommandSuggestions())
            inputText.isMentionMessage() -> suggestionListView.setSuggestions(inputText.getMentionSuggestions())
            else -> suggestionListView.clearSuggestions()
        }
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

    companion object {
        private val COMMAND_PATTERN = Pattern.compile("^/[a-z]*$")
        private val MENTION_PATTERN = Pattern.compile("^(.* )?@([a-zA-Z]+[0-9]*)*$")
    }
}
