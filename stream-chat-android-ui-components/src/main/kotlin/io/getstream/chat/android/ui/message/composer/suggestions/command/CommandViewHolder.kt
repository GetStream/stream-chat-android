package io.getstream.chat.android.ui.message.composer.suggestions.command

import io.getstream.chat.android.client.models.Command
import io.getstream.chat.android.ui.R
import io.getstream.chat.android.ui.common.internal.SimpleListAdapter
import io.getstream.chat.android.ui.databinding.StreamUiItemCommandBinding

/**
 * [RecyclerView.ViewHolder] used for rendering command. Used by [CommandsAdapter].
 *
 * @param binding Handle to [StreamUiItemCommandBinding] instance.
 * @param onCommandSelected Callback invoked when command suggestion item is clicked.
 */
internal class CommandViewHolder(
    val binding: StreamUiItemCommandBinding,
    private val onCommandSelected: (Command) -> Unit,
) :
    SimpleListAdapter.ViewHolder<Command>(binding.root) {

    /**
     * Updates [itemView] elements for a given [Command] object.
     *
     * @param command Single command suggestion represented by [Command] class.
     */
    override fun bind(command: Command) {
        binding.apply {
            root.setOnClickListener { onCommandSelected(command) }
            commandNameTextView.text = command.name.replaceFirstChar(Char::uppercase)
            commandQueryTextView.text = itemView.context.getString(
                R.string.stream_ui_message_input_command_template,
                command.name,
                command.args
            )
        }
    }
}
