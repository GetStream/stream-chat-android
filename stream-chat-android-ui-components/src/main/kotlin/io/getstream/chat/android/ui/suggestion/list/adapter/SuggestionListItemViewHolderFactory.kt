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

package io.getstream.chat.android.ui.suggestion.list.adapter

import android.view.ViewGroup
import io.getstream.chat.android.ui.suggestion.list.SuggestionListViewStyle
import io.getstream.chat.android.ui.suggestion.list.adapter.viewholder.BaseSuggestionItemViewHolder
import io.getstream.chat.android.ui.suggestion.list.adapter.viewholder.internal.CommandViewHolder
import io.getstream.chat.android.ui.suggestion.list.adapter.viewholder.internal.MentionViewHolder

/**
 * A factory that creates ViewHolders used for displaying suggestion list items.
 */
public open class SuggestionListItemViewHolderFactory {

    /**
     * Style used by the suggestion list ViewHolders.
     */
    internal lateinit var style: SuggestionListViewStyle

    /**
     * Creates the ViewHolder used for displaying a list
     * of mention suggestion items.
     */
    public open fun createMentionViewHolder(
        parentView: ViewGroup,
    ): BaseSuggestionItemViewHolder<SuggestionListItem.MentionItem> {
        return MentionViewHolder(
            parent = parentView,
            usernameStyle = style.mentionsUsernameTextStyle,
            mentionNameStyle = style.mentionsNameTextStyle,
            mentionIcon = style.mentionIcon,
        )
    }

    /**
     * Creates the ViewHolder used for displaying a list
     * of command suggestion items.
     */
    public open fun createCommandViewHolder(
        parentView: ViewGroup,
    ): BaseSuggestionItemViewHolder<SuggestionListItem.CommandItem> {
        return CommandViewHolder(
            parent = parentView,
            commandsNameStyle = style.commandsNameTextStyle,
            commandsDescriptionStyle = style.commandsDescriptionTextStyle,
            commandIcon = style.commandIcon,
        )
    }
}
