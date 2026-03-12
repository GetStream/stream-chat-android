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

package io.getstream.chat.android.client.internal.state.plugin.state.channel.internal

import io.getstream.chat.android.models.Message
import io.getstream.chat.android.models.MessageType
import io.getstream.chat.android.models.SyncStatus

/**
 * Returns true if this message is local-only and must be preserved across server message
 * window replacements. Local-only messages are never returned by the server after the
 * initial send attempt completes.
 *
 * Covers:
 * - Pending sends: SYNC_NEEDED, IN_PROGRESS
 * - Attachment upload in-flight: AWAITING_ATTACHMENTS
 * - Send failed: FAILED_PERMANENTLY (user must see to retry or dismiss)
 * - Ephemeral: type == "ephemeral" (e.g. Giphy previews — server never returns these)
 * - Error type: type == "error" (client-generated, not re-delivered by server)
 *
 * DOES NOT include COMPLETED messages — those are already in the server response.
 */
internal fun Message.isLocalOnly(): Boolean =
    syncStatus in setOf(
        SyncStatus.SYNC_NEEDED, // new message or pending edit/delete
        SyncStatus.IN_PROGRESS, // send in flight
        SyncStatus.AWAITING_ATTACHMENTS, // attachment upload pending
        SyncStatus.FAILED_PERMANENTLY, // permanent failure — user must see to retry
    ) || type == MessageType.EPHEMERAL // Giphy preview etc. — never server-returned
        || type == MessageType.ERROR // error type — not re-delivered by server
