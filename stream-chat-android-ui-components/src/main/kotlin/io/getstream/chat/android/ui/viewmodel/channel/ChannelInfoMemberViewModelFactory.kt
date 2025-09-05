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

package io.getstream.chat.android.ui.viewmodel.channel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

/**
 * Factory for creating instances of [ChannelInfoMemberViewModel].
 *
 * @param cid The full channel identifier (e.g., "messaging:123").
 * @param memberId The member ID of the user.
 */
public class ChannelInfoMemberViewModelFactory(
    private val cid: String,
    private val memberId: String,
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        require(modelClass == ChannelInfoMemberViewModel::class.java) {
            "ChannelInfoMemberViewModelFactory can only create instances of ChannelInfoMemberViewModel"
        }
        @Suppress("UNCHECKED_CAST")
        return ChannelInfoMemberViewModel(cid, memberId) as T
    }
}
