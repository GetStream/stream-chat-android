package io.getstream.chat.android.ui.suggestion.list.adapter.internal

import android.view.ViewGroup
import io.getstream.chat.android.client.models.User
import io.getstream.chat.android.ui.common.internal.SimpleListAdapter
import io.getstream.chat.android.ui.suggestion.list.adapter.SuggestionListItem
import io.getstream.chat.android.ui.suggestion.list.adapter.SuggestionListItemViewHolderFactory
import io.getstream.chat.android.ui.suggestion.list.adapter.viewholder.BaseSuggestionItemViewHolder

internal class MentionListAdapter(
    private val viewHolderFactoryProvider: () -> SuggestionListItemViewHolderFactory,
    private val mentionClickListener: (User) -> Unit,
) : SimpleListAdapter<SuggestionListItem.MentionItem, MentionListAdapter.MentionViewHolderWrapper>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MentionViewHolderWrapper {
        return MentionViewHolderWrapper(
            viewHolderFactoryProvider().createMentionViewHolder(parent),
            mentionClickListener
        )
    }

    class MentionViewHolderWrapper(
        private val viewHolder: BaseSuggestionItemViewHolder<SuggestionListItem.MentionItem>,
        private val mentionClickListener: (User) -> Unit,
    ) : SimpleListAdapter.ViewHolder<SuggestionListItem.MentionItem>(viewHolder.itemView) {

        private lateinit var user: User

        init {
            viewHolder.itemView.setOnClickListener { mentionClickListener(user) }
        }

        override fun bind(command: SuggestionListItem.MentionItem) {
            this.user = command.user
            viewHolder.bindItem(command)
        }
    }
}
