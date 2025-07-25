/*
 * Copyright (c) 2014-2025 Stream.io Inc. All rights reserved.
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

package io.getstream.chat.android.compose.viewmodel.mentions

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import io.getstream.chat.android.models.Message
import io.getstream.chat.android.models.querysort.QuerySorter

/**
 * Factory for creating a [MentionListViewModel] with a custom sort.
 *
 * @param sort The sorting options for the messages.
 */
public class MentionListViewModelFactory @JvmOverloads constructor(
    private val sort: QuerySorter<Message>? = null,
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        require(modelClass == MentionListViewModel::class.java) {
            "MentionListViewModelFactory can only create instances of MentionListViewModel"
        }

        @Suppress("UNCHECKED_CAST")
        return MentionListViewModel(sort = sort) as T
    }
}
