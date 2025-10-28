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

package io.getstream.chat.android.ui.viewmodel.channels.internal

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

/**
 * Specialized factory class that produces [ChannelActionsViewModel].
 *
 * @param cid The full channel id, i.e. "messaging:123".
 */
internal class ChannelActionsViewModelFactory(
    private val cid: String,
) : ViewModelProvider.Factory {

    /**
     * Creates an instance of [ChannelActionsViewModel].
     */
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T = ChannelActionsViewModel(cid) as T
}
