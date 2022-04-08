/*
 * Copyright (c) 2014-2022 Stream.io Inc. All rights reserved.
 *
 * Licensed under the Stream License;
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    https://github.com/GetStream/stream-chat-android/blob/main/LICENSE
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
    commandsNameStyle: TextStyle,
    commandsDescriptionStyle: TextStyle,
    commandIcon: Drawable,
    private val binding: StreamUiItemCommandBinding = StreamUiItemCommandBinding
        .inflate(parent.streamThemeInflater, parent, false),
) : BaseSuggestionItemViewHolder<SuggestionListItem.CommandItem>(binding.root) {

    init {
        commandsNameStyle.apply(binding.commandNameTextView)
        commandsDescriptionStyle.apply(binding.commandQueryTextView)
        binding.instantCommandImageView.setImageDrawable(commandIcon)
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
