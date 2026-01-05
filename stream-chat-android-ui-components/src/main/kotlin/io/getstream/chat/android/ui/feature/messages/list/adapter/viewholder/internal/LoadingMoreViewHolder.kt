/*
 * Copyright (c) 2014-2026 Stream.io Inc. All rights reserved.
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

package io.getstream.chat.android.ui.feature.messages.list.adapter.viewholder.internal

import android.view.ViewGroup
import io.getstream.chat.android.ui.feature.messages.list.MessageListItemStyle
import io.getstream.chat.android.ui.feature.messages.list.adapter.BaseMessageItemViewHolder
import io.getstream.chat.android.ui.feature.messages.list.adapter.MessageListItem
import io.getstream.chat.android.ui.feature.messages.list.adapter.MessageListItemPayloadDiff
import io.getstream.chat.android.ui.utils.extensions.streamThemeInflater

/**
 * ViewHolder used for displaying loading more indicator.
 *
 * @param parent The parent container.
 * @param style Style for view holders.
 */
internal class LoadingMoreViewHolder(
    parent: ViewGroup,
    style: MessageListItemStyle,
) : BaseMessageItemViewHolder<MessageListItem.LoadingMoreIndicatorItem>(
    parent.streamThemeInflater.inflate(style.loadingMoreView, parent, false),
) {

    override fun bindData(data: MessageListItem.LoadingMoreIndicatorItem, diff: MessageListItemPayloadDiff) = Unit
}
