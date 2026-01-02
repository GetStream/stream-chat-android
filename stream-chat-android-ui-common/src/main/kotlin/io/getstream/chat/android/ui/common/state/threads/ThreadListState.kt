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

package io.getstream.chat.android.ui.common.state.threads

import io.getstream.chat.android.models.Thread

/**
 * Represents the Threads list state, used to render the Threads list UI.
 *
 * @param threads The list of loaded [Thread]s.
 * @param isLoading Indicator if the threads are loading.
 * @param isLoadingMore Indicator if there is loading of the next page of threads in progress.
 * @param unseenThreadsCount The number of threads that we know that exist, but are not (yet) loaded in the list.
 */
public data class ThreadListState(
    val threads: List<Thread>,
    val isLoading: Boolean,
    val isLoadingMore: Boolean,
    val unseenThreadsCount: Int,
)
