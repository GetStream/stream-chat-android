package io.getstream.chat.android.ui.suggestion.list.adapter

import android.view.ViewGroup
import io.getstream.chat.android.ui.suggestion.list.SuggestionListViewStyle
import io.getstream.chat.android.ui.suggestion.list.adapter.viewholder.BaseSuggestionItemViewHolder
import io.getstream.chat.android.ui.suggestion.list.adapter.viewholder.internal.CommandViewHolder
import io.getstream.chat.android.ui.suggestion.list.adapter.viewholder.internal.MentionViewHolder

public open class SuggestionListItemViewHolderFactory {
    internal var style: SuggestionListViewStyle? = null

    public open fun createMentionViewHolder(
        parentView: ViewGroup,
    ): BaseSuggestionItemViewHolder<SuggestionListItem.MentionItem> {
        return MentionViewHolder(
            parent = parentView,
            usernameStyle = style?.mentionsUsernameTextStyle,
            mentionNameStyle = style?.mentionsNameTextStyle,
            mentionIcon = style?.mentionIcon,
        )
    }

    public open fun createCommandViewHolder(
        parentView: ViewGroup,
    ): BaseSuggestionItemViewHolder<SuggestionListItem.CommandItem> {
        return CommandViewHolder(
            parent = parentView,
            commandsNameStyle = style?.commandsNameTextStyle,
            commandsDescriptionStyle = style?.commandsDescriptionTextStyle,
            commandIcon = style?.commandIcon
        )
    }
}
