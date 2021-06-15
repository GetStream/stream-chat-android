package io.getstream.chat.android.ui.suggestion.list.adapter

import io.getstream.chat.android.client.models.Command
import io.getstream.chat.android.client.models.User
import io.getstream.chat.android.ui.suggestion.list.SuggestionListView

/**
 * Represents elements that are displayed in [SuggestionListView]
 */
public sealed class SuggestionListItem {
    public data class MentionItem(val user: User) : SuggestionListItem()
    public data class CommandItem(val command: Command) : SuggestionListItem()
}
