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
import io.getstream.chat.android.ui.databinding.StreamUiItemMentionBinding
import io.getstream.chat.android.ui.suggestion.list.adapter.SuggestionListItem
import io.getstream.chat.android.ui.suggestion.list.adapter.viewholder.BaseSuggestionItemViewHolder

internal class MentionViewHolder(
    parent: ViewGroup,
    usernameStyle: TextStyle,
    mentionNameStyle: TextStyle,
    mentionIcon: Drawable,
    private val binding: StreamUiItemMentionBinding = StreamUiItemMentionBinding
        .inflate(parent.streamThemeInflater, parent, false),
) : BaseSuggestionItemViewHolder<SuggestionListItem.MentionItem>(binding.root) {

    init {
        usernameStyle.apply(binding.usernameTextView)
        mentionNameStyle.apply(binding.mentionNameTextView)
        binding.mentionsIcon.setImageDrawable(mentionIcon)
    }

    override fun bindItem(item: SuggestionListItem.MentionItem) {
        val user = item.user
        binding.apply {
            avatarView.setUserData(user)
            usernameTextView.text = user.name
            mentionNameTextView.text = itemView.context.getString(
                R.string.stream_ui_mention,
                user.name.lowercase()
            )
        }
    }
}
