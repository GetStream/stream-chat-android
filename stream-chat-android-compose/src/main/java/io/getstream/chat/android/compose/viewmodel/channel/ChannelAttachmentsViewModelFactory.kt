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

package io.getstream.chat.android.compose.viewmodel.channel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import io.getstream.chat.android.models.Attachment

/**
 * Factory for creating instances of [ChannelAttachmentsViewModel].
 *
 * @param cid The full channel identifier (e.g., "messaging:123").
 * @param attachmentTypes The list of attachment types (e.g., "image", "file").
 * @param localFilter A function to filter attachments locally after fetching.
 */
public class ChannelAttachmentsViewModelFactory(
    private val cid: String,
    private val attachmentTypes: List<String>,
    private val localFilter: (attachment: Attachment) -> Boolean = { true },
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        require(modelClass == ChannelAttachmentsViewModel::class.java) {
            "ChannelAttachmentsViewModelFactory can only create instances of ChannelAttachmentsViewModel"
        }
        @Suppress("UNCHECKED_CAST")
        return ChannelAttachmentsViewModel(cid, attachmentTypes, localFilter) as T
    }
}
