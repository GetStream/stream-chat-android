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

package io.getstream.chat.android.ui.feature.threads.list.adapter.viewholder

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import io.getstream.chat.android.ui.feature.threads.list.adapter.ThreadListItem
import io.getstream.log.taggedLogger

/**
 * Base ViewHolder used for displaying items by the
 * [io.getstream.chat.android.ui.feature.threads.list.adapter.internal.ThreadListAdapter].
 */
public abstract class BaseThreadListItemViewHolder<T : ThreadListItem>(itemView: View) :
    RecyclerView.ViewHolder(itemView) {

    private val logger by taggedLogger("Chat:BaseThreadListItemViewHolder")

    /**
     * Binds the item to the ViewHolder.
     *
     * @param item The item to bind.
     */
    public abstract fun bind(item: T)

    /**
     * Workaround to allow a downcast of the [ThreadListItem] to [T].
     */
    @Suppress("UNCHECKED_CAST")
    internal fun bindInternal(item: ThreadListItem) {
        val actual = item as? T
        if (actual == null) {
            // Should never happen
            logger.d { "[bindInternal] Failed to cast $item to the expected type." }
            return
        }
        bind(actual)
    }
}
