package io.getstream.chat.ui.sample.feature.chat

import android.view.LayoutInflater
import android.view.ViewGroup
import io.getstream.chat.android.ui.databinding.StreamUiItemCommandBinding
import io.getstream.chat.android.ui.suggestion.list.adapter.SuggestionListItem
import io.getstream.chat.android.ui.suggestion.list.adapter.SuggestionListItemViewHolderFactory
import io.getstream.chat.android.ui.suggestion.list.adapter.viewholder.BaseSuggestionItemViewHolder

class CustomSuggestionListViewHolderFactory : SuggestionListItemViewHolderFactory() {

    override fun createCommandViewHolder(
        parentView: ViewGroup,
    ): BaseSuggestionItemViewHolder<SuggestionListItem.CommandItem> {
        return CustomCommandViewHolder(parentView)
    }
}

internal class CustomCommandViewHolder(
    parent: ViewGroup,
    private val binding: StreamUiItemCommandBinding = StreamUiItemCommandBinding
        .inflate(LayoutInflater.from(parent.context), parent, false),
) : BaseSuggestionItemViewHolder<SuggestionListItem.CommandItem>(binding.root) {

    override fun bindItem(data: SuggestionListItem.CommandItem) {
        binding.apply {
            commandNameTextView.text = "NAME"
            commandQueryTextView.text = "QUERY"
        }
    }
}
