package io.getstream.chat.android.ui.suggestion.list.adapter.internal

import android.view.ViewGroup
import io.getstream.chat.android.client.models.Command
import io.getstream.chat.android.ui.common.internal.SimpleListAdapter
import io.getstream.chat.android.ui.suggestion.list.adapter.SuggestionListItem
import io.getstream.chat.android.ui.suggestion.list.adapter.SuggestionListItemViewHolderFactory
import io.getstream.chat.android.ui.suggestion.list.adapter.viewholder.BaseSuggestionItemViewHolder

internal class CommandListAdapter(
    private val factoryProvider: () -> SuggestionListItemViewHolderFactory,
    private val commandClickListener: (Command) -> Unit,
) : SimpleListAdapter<SuggestionListItem.CommandItem, CommandListAdapter.CommandViewHolderWrapper>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CommandViewHolderWrapper {
        return CommandViewHolderWrapper(factoryProvider().createCommandViewHolder(parent), commandClickListener)
    }

    class CommandViewHolderWrapper(
        private val viewHolder: BaseSuggestionItemViewHolder<SuggestionListItem.CommandItem>,
        private val commandClickListener: (Command) -> Unit,
    ) : SimpleListAdapter.ViewHolder<SuggestionListItem.CommandItem>(viewHolder.itemView) {

        private lateinit var command: Command

        init {
            viewHolder.itemView.setOnClickListener { commandClickListener(command) }
        }

        override fun bind(item: SuggestionListItem.CommandItem) {
            this.command = item.command
            viewHolder.bindItem(item)
        }
    }
}
