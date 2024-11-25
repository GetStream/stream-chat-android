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

package io.getstream.chat.android.ui.viewmodel.threads

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import io.getstream.chat.android.ui.common.feature.threads.ThreadListController

/**
 * A ViewModel factory for creating a [ThreadListViewModel].
 *
 * @see ThreadListViewModel
 *
 * @param threadLimit The number of threads to load per page.
 * @param threadReplyLimit The number of replies per thread to load.
 * @param threadParticipantLimit The number of participants per thread to load.
 */
public class ThreadsViewModelFactory(
    private val threadLimit: Int = ThreadListController.DEFAULT_THREAD_LIMIT,
    private val threadReplyLimit: Int = ThreadListController.DEFAULT_THREAD_REPLY_LIMIT,
    private val threadParticipantLimit: Int = ThreadListController.DEFAULT_THREAD_PARTICIPANT_LIMIT,
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        require(modelClass == ThreadListViewModel::class.java) {
            "ThreadsViewModelFactory can only create instances of ThreadListViewModel"
        }
        @Suppress("UNCHECKED_CAST")
        return ThreadListViewModel(
            controller = ThreadListController(
                threadLimit = threadLimit,
                threadReplyLimit = threadReplyLimit,
                threadParticipantLimit = threadParticipantLimit,
            ),
        ) as T
    }
}
