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

package io.getstream.chat.android.state.plugin.state.querythreads

import io.getstream.chat.android.models.FilterObject
import io.getstream.chat.android.models.Thread
import io.getstream.chat.android.models.querysort.QuerySorter
import kotlinx.coroutines.flow.StateFlow

/**
 * Contains the state related to threads queries.
 */
public interface QueryThreadsState {

    /** The filter associated with the query threads state. */
    public val filter: FilterObject?

    /** The sort object associated with the query threads state. */
    public val sort: QuerySorter<Thread>

    /** Sorted list of [Thread]s. */
    public val threads: StateFlow<List<Thread>>

    /** Indicator if the current state is being loaded. */
    public val loading: StateFlow<Boolean>

    /** Indicator if the current state is loading more threads (a next page is being loaded). */
    public val loadingMore: StateFlow<Boolean>

    /**
     * The identifier for the next page of threads.
     * null-value represents that the last page is loaded and there are no more threads to load.
     */
    public val next: StateFlow<String?>

    /** The IDs of the threads which exist, but are not (yet) loaded in the paginated list of threads. */
    public val unseenThreadIds: StateFlow<Set<String>>
}
