package io.getstream.chat.android.ui.suggestion.internal

import android.view.ViewGroup
import io.getstream.chat.android.client.models.Command
import io.getstream.chat.android.ui.R
import io.getstream.chat.android.ui.common.extensions.internal.streamThemeInflater
import io.getstream.chat.android.ui.common.internal.SimpleListAdapter
import io.getstream.chat.android.ui.common.style.TextStyle
import io.getstream.chat.android.ui.databinding.StreamUiItemCommandBinding

internal class CommandsAdapter(
    var commandsNameStyle: TextStyle? = null,
    var commandsDescriptionStyle: TextStyle? = null,
    private val onCommandSelected: (Command) -> Unit,
) : SimpleListAdapter<Command, CommandsAdapter.CommandViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CommandViewHolder {
        return StreamUiItemCommandBinding
            .inflate(parent.streamThemeInflater, parent, false)
            .let { binding ->
                commandsNameStyle?.apply(binding.commandNameTextView)
                commandsDescriptionStyle?.apply(binding.commandQueryTextView)

                CommandViewHolder(binding, onCommandSelected)
            }
    }

    class CommandViewHolder(
        private val binding: StreamUiItemCommandBinding,
        private val onCommandClicked: (Command) -> Unit,
    ) : SimpleListAdapter.ViewHolder<Command>(binding.root) {

        lateinit var command: Command

        init {
            binding.root.setOnClickListener { onCommandClicked(command) }
        }

        override fun bind(command: Command) {
            this.command = command
            binding.apply {
                commandNameTextView.text = command.name.capitalize()
                commandQueryTextView.text = itemView.context.getString(
                    R.string.stream_ui_command_command_template,
                    command.name,
                    command.args
                )
            }
        }
    }
}
