package io.getstream.chat.android.ui.message.composer.suggestions.mention

import android.view.ViewGroup
import io.getstream.chat.android.client.models.User
import io.getstream.chat.android.ui.common.extensions.internal.streamThemeInflater
import io.getstream.chat.android.ui.common.internal.SimpleListAdapter
import io.getstream.chat.android.ui.databinding.StreamUiItemMentionBinding
import io.getstream.chat.android.ui.message.composer.suggestions.mention.MentionsViewHolder

/**
 * [RecyclerView.Adapter] responsible for displaying mention suggestions in the RecyclerView in [DefaultMentionSuggestionsContent].
 */
internal class MentionsAdapter(private inline val onMentionSelected: (User) -> Unit) :
    SimpleListAdapter<User, MentionsViewHolder>() {

    /**
     * Inflates layout and instantiates [MentionsViewHolder].
     *
     * @param parent Container item layout provided by [RecyclerView.Adapter].
     * @param viewType View type provided by [RecyclerView.Adapter]. In case of this [MentionsAdapter] it's not used.
     *
     * @return [MentionsViewHolder] instance.
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MentionsViewHolder {
        val binding = StreamUiItemMentionBinding.inflate(parent.streamThemeInflater, parent, false)
        return MentionsViewHolder(binding, onMentionSelected)
    }
}