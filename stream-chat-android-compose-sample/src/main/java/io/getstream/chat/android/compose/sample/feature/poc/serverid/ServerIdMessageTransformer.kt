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

package io.getstream.chat.android.compose.sample.feature.poc.serverid

import android.util.Log
import io.getstream.chat.android.client.interceptor.message.PreSendMessageTransformer
import io.getstream.chat.android.core.ExperimentalStreamChatApi
import io.getstream.chat.android.models.Message

/**
 * A [PreSendMessageTransformer] that rewrites the outgoing message id to the backend-assigned id delivered by
 * [ServerIdFileUploader] via the attachments' extra data. The original local id is preserved in the message
 * extra data under [ServerIdPoc.KEY_LOCAL_ID], so the UI can keep using it as a stable list key.
 */
@OptIn(ExperimentalStreamChatApi::class)
class ServerIdMessageTransformer : PreSendMessageTransformer {

    override suspend fun transform(message: Message): Message {
        val remoteIds = message.attachments
            .mapNotNull { it.extraData[ServerIdPoc.KEY_REMOTE_MESSAGE_ID] as? String }
            .distinct()
        val remoteId = remoteIds.firstOrNull()
            ?: return message // No attachment carries a remote id: nothing to do.
        if (remoteIds.size > 1) {
            // Multiple attachments were assigned different message ids by the backend. Which one wins is a
            // contract to be defined with the app backend; this PoC simply picks the first.
            Log.w(ServerIdPoc.TAG, "Conflicting remote message ids: $remoteIds; using the first one")
        }
        if (remoteId == message.id) return message // Already rewritten (e.g. this send is a retry).
        Log.d(ServerIdPoc.TAG, "Rewriting message id: ${message.id} -> $remoteId")
        return message.copy(
            id = remoteId,
            extraData = message.extraData + (ServerIdPoc.KEY_LOCAL_ID to message.id),
        )
    }
}
