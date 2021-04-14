package io.getstream.chat.android.ui.suggestion

import io.getstream.chat.android.client.models.Command
import io.getstream.chat.android.client.models.User

public sealed class Suggestions {

    public fun hasSuggestions(): Boolean {
        return when (this) {
            is CommandSuggestions -> commands.isNotEmpty()
            is MentionSuggestions -> users.isNotEmpty()
            is EmptySuggestions -> false
        }
    }

    public data class MentionSuggestions(val users: List<User>) : Suggestions()
    public data class CommandSuggestions(val commands: List<Command>) : Suggestions()
    public object EmptySuggestions : Suggestions()
}
