package io.getstream.chat.android.ui.suggestion.list.adapter.viewholder.internal

import android.graphics.drawable.Drawable
import android.view.ViewGroup
import io.getstream.chat.android.ui.R
import io.getstream.chat.android.ui.common.extensions.internal.streamThemeInflater
import io.getstream.chat.android.ui.common.style.TextStyle
import io.getstream.chat.android.ui.databinding.StreamUiItemCommandBinding
import io.getstream.chat.android.ui.suggestion.list.adapter.SuggestionListItem
import io.getstream.chat.android.ui.suggestion.list.adapter.viewholder.BaseSuggestionItemViewHolder

internal class CommandViewHolder(
    parent: ViewGroup,
    commandsNameStyle: TextStyle? = null,
    commandsDescriptionStyle: TextStyle? = null,
    commandIcon: Drawable? = null,
    private val binding: StreamUiItemCommandBinding = StreamUiItemCommandBinding
        .inflate(parent.streamThemeInflater, parent, false),
) : BaseSuggestionItemViewHolder<SuggestionListItem.CommandItem>(binding.root) {

    init {
        commandsNameStyle?.apply(binding.commandNameTextView)
        commandsDescriptionStyle?.apply(binding.commandQueryTextView)
        commandIcon?.let(binding.instantCommandImageView::setImageDrawable)
    }

    override fun bindItem(item: SuggestionListItem.CommandItem) {
        val command = item.command
        binding.apply {
            commandNameTextView.text = command.name.replaceFirstChar(Char::uppercase)
            commandQueryTextView.text = itemView.context.getString(
                R.string.stream_ui_message_input_command_template,
                command.name,
                command.args
            )
        }
    }
}
