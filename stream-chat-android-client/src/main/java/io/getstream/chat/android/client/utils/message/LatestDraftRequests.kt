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

package io.getstream.chat.android.client.utils.message

import io.getstream.chat.android.models.DraftMessage
import java.util.concurrent.ConcurrentHashMap

/**
 * Remembers the latest request to save or delete the draft of each channel or thread, so the result of an older
 * request does not settle a newer local change.
 */
internal class LatestDraftRequests {

    private val latest = ConcurrentHashMap<String, Pair<DraftMessage, Boolean>>()

    /** Records [message] as the latest request of its draft. */
    fun onRequest(message: DraftMessage, deleted: Boolean) {
        latest[message.draftKey()] = message to deleted
    }

    /** Returns `true` when [message] is still the latest request of its draft, and forgets it. */
    fun finish(message: DraftMessage, deleted: Boolean): Boolean = latest.remove(message.draftKey(), message to deleted)

    private fun DraftMessage.draftKey(): String = "$cid/${parentId.orEmpty()}"
}
