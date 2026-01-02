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

package io.getstream.chat.android.ui.viewmodel.channel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import io.getstream.chat.android.ui.common.helper.CopyToClipboardHandler
import io.getstream.chat.android.ui.common.state.channel.info.ChannelInfoViewState

/**
 * Factory for creating instances of [ChannelInfoViewModel].
 *
 * @param context The application context.
 * @param cid The full channel identifier (Ex. "messaging:123").
 * @param optionFilter A filter function for channel options, allowing customization of which options are displayed.
 *                      Defaults to a function that returns true for all options.
 */
public class ChannelInfoViewModelFactory(
    private val context: Context,
    private val cid: String,
    private val optionFilter: (option: ChannelInfoViewState.Content.Option) -> Boolean = { true },
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        require(modelClass == ChannelInfoViewModel::class.java) {
            "ChannelInfoViewModelFactory can only create instances of ChannelInfoViewModel"
        }
        @Suppress("UNCHECKED_CAST")
        return ChannelInfoViewModel(
            cid = cid,
            copyToClipboardHandler = CopyToClipboardHandler(context = context.applicationContext),
            optionFilter = optionFilter,
        ) as T
    }
}
