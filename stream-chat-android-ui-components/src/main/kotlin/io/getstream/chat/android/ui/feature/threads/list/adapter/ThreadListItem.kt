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

package io.getstream.chat.android.ui.feature.threads.list.adapter

import io.getstream.chat.android.models.Thread

/**
 * Class representing the different types of items that can be rendered in the
 * by the [io.getstream.chat.android.ui.feature.threads.list.adapter.internal.ThreadListAdapter].
 */
public sealed interface ThreadListItem {

    /**
     * The item stable ID.
     */
    public val stableId: Long

    /**
     * Represents a thread item.
     */
    public data class ThreadItem(val thread: Thread) : ThreadListItem {
        override val stableId: Long
            get() = thread.parentMessage.identifierHash()
    }

    /**
     * Represents a loading more item.
     */
    public data object LoadingMoreItem : ThreadListItem {
        override val stableId: Long
            get() = LOADING_MORE_ITEM_STABLE_ID
    }

    private companion object {
        private const val LOADING_MORE_ITEM_STABLE_ID = 1L
    }
}
