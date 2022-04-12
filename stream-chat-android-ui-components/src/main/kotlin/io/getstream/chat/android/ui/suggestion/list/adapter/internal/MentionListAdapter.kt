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

        override fun bind(item: SuggestionListItem.MentionItem) {
            this.user = item.user
            viewHolder.bindItem(item)
        }
    }
}
