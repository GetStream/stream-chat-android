package io.getstream.chat.android.ui.suggestion.internal

import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.getstream.sdk.chat.utils.extensions.inflater
import io.getstream.chat.android.client.models.Command
import io.getstream.chat.android.ui.R
import io.getstream.chat.android.ui.databinding.StreamUiItemCommandBinding

internal class CommandsAdapter(
    private val onCommandSelected: (Command) -> Unit
) : ListAdapter<Command, CommandsAdapter.CommandViewHolder>(
    object : DiffUtil.ItemCallback<Command>() {
        override fun areItemsTheSame(oldItem: Command, newItem: Command): Boolean {
            return oldItem.name == newItem.name
        }

        override fun areContentsTheSame(oldItem: Command, newItem: Command): Boolean {
            return oldItem == newItem
        }
    }
) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CommandViewHolder {
        return StreamUiItemCommandBinding
            .inflate(parent.inflater, parent, false)
            .let { CommandViewHolder(it, onCommandSelected) }
    }

    override fun onBindViewHolder(holder: CommandViewHolder, position: Int) {
        return holder.bind(getItem(position))
    }

    class CommandViewHolder(
        private val binding: StreamUiItemCommandBinding,
        private val onCommandClicked: (Command) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        lateinit var command: Command

        init {
            binding.root.setOnClickListener { onCommandClicked(command) }
        }

        fun bind(command: Command) {
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
