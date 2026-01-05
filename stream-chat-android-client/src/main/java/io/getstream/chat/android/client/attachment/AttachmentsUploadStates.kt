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

package io.getstream.chat.android.client.attachment

import io.getstream.chat.android.models.Attachment
import io.getstream.chat.android.models.Message
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

internal object AttachmentsUploadStates {

    private var messagesStates: Map<String, MutableStateFlow<List<Attachment>>> = emptyMap()

    fun updateMessageAttachments(message: Message) {
        synchronized(this) {
            val attachmentsStateFlow = messagesStates.getOrElse(message.id) { MutableStateFlow(message.attachments) }
            attachmentsStateFlow.value = message.attachments
            messagesStates = messagesStates + (message.id to attachmentsStateFlow)
        }
    }

    fun observeAttachments(messageId: String): Flow<List<Attachment>> {
        synchronized(this) {
            return messagesStates.getOrElse(messageId) { MutableStateFlow(emptyList()) }
        }
    }

    fun removeMessageAttachmentsState(messageId: String) {
        synchronized(this) {
            messagesStates = messagesStates - messageId
        }
    }

    fun clearStates() {
        synchronized(this) {
            messagesStates = emptyMap()
        }
    }
}
