/*
 * Copyright (c) 2014-2024 Stream.io Inc. All rights reserved.
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

package io.getstream.chat.android.ui.feature.threads.list.adapter.viewholder.internal

import android.view.ViewGroup
import io.getstream.chat.android.ui.databinding.StreamUiItemListLoadingBinding
import io.getstream.chat.android.ui.feature.threads.list.adapter.ThreadListItem
import io.getstream.chat.android.ui.feature.threads.list.adapter.viewholder.BaseThreadListItemViewHolder
import io.getstream.chat.android.ui.utils.extensions.streamThemeInflater

/**
 * ViewHolder for the thread list loading more indicator item.
 */
internal class ThreadListLoadingMoreViewHolder(
    parentView: ViewGroup,
) : BaseThreadListItemViewHolder<ThreadListItem.LoadingMoreItem>(
    itemView = StreamUiItemListLoadingBinding.inflate(parentView.streamThemeInflater, parentView, false).root,
) {

    override fun bind(item: ThreadListItem.LoadingMoreItem) = Unit // No-op
}
