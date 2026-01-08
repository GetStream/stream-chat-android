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

package io.getstream.chat.android.compose.viewmodel.channel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import io.getstream.chat.android.compose.util.AttachmentFileController

internal class ChannelMediaAttachmentsPreviewViewModelFactory(
    private val context: Context,
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        require(modelClass == ChannelMediaAttachmentsPreviewViewModel::class.java) {
            "ChannelMediaAttachmentsPreviewViewModelFactory can only create instances of " +
                "ChannelMediaAttachmentsPreviewViewModel"
        }
        @Suppress("UNCHECKED_CAST")
        return ChannelMediaAttachmentsPreviewViewModel(
            attachmentFileController = AttachmentFileController(
                context = context.applicationContext,
            ),
        ) as T
    }
}
