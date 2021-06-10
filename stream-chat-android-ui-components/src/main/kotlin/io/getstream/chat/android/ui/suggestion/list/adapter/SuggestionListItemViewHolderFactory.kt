package io.getstream.chat.android.ui.suggestion.list.adapter

import android.view.ViewGroup
import io.getstream.chat.android.ui.suggestion.list.adapter.viewholder.BaseSuggestionItemViewHolder
import io.getstream.chat.android.ui.suggestion.list.adapter.viewholder.internal.CommandViewHolder
import io.getstream.chat.android.ui.suggestion.list.adapter.viewholder.internal.MentionViewHolder
import io.getstream.chat.android.ui.suggestion.list.internal.SuggestionListViewStyle

public open class SuggestionListItemViewHolderFactory {
    internal var style: SuggestionListViewStyle? = null

    public open fun createMentionViewHolder(
        parentView: ViewGroup,
    ): BaseSuggestionItemViewHolder<SuggestionListItem.MentionItem> {
        return MentionViewHolder(
            parentView,
            style?.mentionsUsernameTextStyle,
            style?.mentionsNameTextStyle,
            style?.mentionIcon
        )
    }

    public open fun createCommandViewHolder(
        parentView: ViewGroup,
    ): BaseSuggestionItemViewHolder<SuggestionListItem.CommandItem> {
        return CommandViewHolder(
            parentView,
            style?.commandsNameTextStyle,
            style?.commandsDescriptionStyle
        )
    }
}
