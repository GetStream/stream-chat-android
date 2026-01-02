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

package io.getstream.chat.android.compose.viewmodel.threads

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.api.models.QueryThreadsRequest
import io.getstream.chat.android.ui.common.feature.threads.ThreadListController

/**
 * A ViewModel factory for creating a [ThreadListViewModel].
 *
 * @see ThreadListViewModel
 *
 * @param query The [QueryThreadsRequest] used to load threads.
 * @param chatClient The [ChatClient] instance to use for loading threads.
 */
public class ThreadsViewModelFactory(
    private val query: QueryThreadsRequest,
    private val chatClient: ChatClient = ChatClient.instance(),
) : ViewModelProvider.Factory {

    /**
     * Creates a factory instance with the specified parameters.
     *
     * @see ThreadListViewModel
     *
     * @param threadLimit The number of threads to load per page.
     * @param threadReplyLimit The number of replies per thread to load.
     * @param threadParticipantLimit The number of participants per thread to load.
     * @param chatClient The [ChatClient] instance to use for loading threads.
     */
    @Deprecated(
        message = "Use ThreadsViewModelFactory(QueryThreadsRequest) instead, to provide more query options such" +
            " as filtering and sorting.",
        level = DeprecationLevel.WARNING,
    )
    public constructor(
        threadLimit: Int = ThreadListController.DEFAULT_THREAD_LIMIT,
        threadReplyLimit: Int = ThreadListController.DEFAULT_THREAD_REPLY_LIMIT,
        threadParticipantLimit: Int = ThreadListController.DEFAULT_THREAD_PARTICIPANT_LIMIT,
        chatClient: ChatClient = ChatClient.instance(),
    ) : this(
        query = QueryThreadsRequest(
            limit = threadLimit,
            replyLimit = threadReplyLimit,
            participantLimit = threadParticipantLimit,
        ),
        chatClient = chatClient,
    )

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        require(modelClass == ThreadListViewModel::class.java) {
            "ThreadsViewModelFactory can only create instances of ThreadListViewModel"
        }
        @Suppress("UNCHECKED_CAST")
        return ThreadListViewModel(controller = ThreadListController(query, chatClient)) as T
    }
}
