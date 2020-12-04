package io.getstream.chat.android.ui.suggestions

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import io.getstream.chat.android.client.models.Command
import io.getstream.chat.android.ui.R
import io.getstream.chat.android.ui.databinding.StreamItemCommandBinding

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
        return StreamItemCommandBinding
            .inflate(LayoutInflater.from(parent.context), parent, false)
            .let { CommandViewHolder(it, onCommandSelected) }
    }

    override fun onBindViewHolder(holder: CommandViewHolder, position: Int) {
        return holder.bind(getItem(position))
    }

    class CommandViewHolder(
        private val binding: StreamItemCommandBinding,
        private val onCommandClicked: (Command) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(command: Command) {
            binding.commandNameTextView.text = command.name.capitalize()
            binding.commandQueryTextView.text = itemView.context.getString(
                R.string.stream_ui_command_command_template,
                command.name,
                command.args
            )
            binding.root.setOnClickListener { onCommandClicked(command) }
        }
    }
}
