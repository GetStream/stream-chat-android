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
        return CommandViewHolderWrapper(
            factoryProvider().createCommandViewHolder(parent),
            commandClickListener
        )
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
