package io.getstream.chat.android.ui.suggestion.list.adapter

import android.view.ViewGroup
import io.getstream.chat.android.ui.suggestion.list.SuggestionListViewStyle
import io.getstream.chat.android.ui.suggestion.list.adapter.viewholder.BaseSuggestionItemViewHolder
import io.getstream.chat.android.ui.suggestion.list.adapter.viewholder.internal.CommandViewHolder
import io.getstream.chat.android.ui.suggestion.list.adapter.viewholder.internal.MentionViewHolder

/**
 * A factory that creates ViewHolders used for displaying suggestion list items.
 */
public open class SuggestionListItemViewHolderFactory {

    /**
     * Style used by the suggestion list ViewHolders.
     */
    internal lateinit var style: SuggestionListViewStyle

    /**
     * Creates the ViewHolder used for displaying a list
     * of mention suggestion items.
     */
    public open fun createMentionViewHolder(
        parentView: ViewGroup,
    ): BaseSuggestionItemViewHolder<SuggestionListItem.MentionItem> {
        return MentionViewHolder(
            parent = parentView,
            usernameStyle = style.mentionsUsernameTextStyle,
            mentionNameStyle = style.mentionsNameTextStyle,
            mentionIcon = style.mentionIcon,
        )
    }

    /**
     * Creates the ViewHolder used for displaying a list
     * of command suggestion items.
     */
    public open fun createCommandViewHolder(
        parentView: ViewGroup,
    ): BaseSuggestionItemViewHolder<SuggestionListItem.CommandItem> {
        return CommandViewHolder(
            parent = parentView,
            commandsNameStyle = style.commandsNameTextStyle,
            commandsDescriptionStyle = style.commandsDescriptionTextStyle,
            commandIcon = style.commandIcon,
        )
    }
}
