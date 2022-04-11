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

package io.getstream.chat.android.ui.pinned.list.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

public class PinnedMessageListViewModelFactory(private val cid: String?) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        require(cid != null) {
            "Channel cid should not be null"
        }
        require(modelClass == PinnedMessageListViewModel::class.java) {
            "PinnedMessageListViewModelFactory can only create instances of PinnedMessageListViewModel"
        }

        @Suppress("UNCHECKED_CAST")
        return PinnedMessageListViewModel(cid) as T
    }
}
