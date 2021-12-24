package io.getstream.chat.android.ui.message.composer.suggestions.command

import android.view.ViewGroup
import io.getstream.chat.android.client.models.Command
import io.getstream.chat.android.ui.common.extensions.internal.streamThemeInflater
import io.getstream.chat.android.ui.common.internal.SimpleListAdapter
import io.getstream.chat.android.ui.databinding.StreamUiItemCommandBinding

/**
 * [RecyclerView.Adapter] responsible for displaying command suggestions in the RecyclerView in [DefaultCommandSuggestionsContent].
 */
internal class CommandsAdapter(private inline val onCommandSelected: (Command) -> Unit) :
    SimpleListAdapter<Command, CommandViewHolder>() {

    /**
     * Inflates layout and instantiates [CommandViewHolder].
     *
     * @param parent Container item layout provided by [RecyclerView.Adapter].
     * @param viewType View type provided by [RecyclerView.Adapter]. In case of this [CommandsAdapter] it's not used.
     *
     * @return [CommandViewHolder] instance.
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CommandViewHolder {
        val binding = StreamUiItemCommandBinding.inflate(parent.streamThemeInflater, parent, false)
        return CommandViewHolder(binding) { onCommandSelected(it) }
    }
}
