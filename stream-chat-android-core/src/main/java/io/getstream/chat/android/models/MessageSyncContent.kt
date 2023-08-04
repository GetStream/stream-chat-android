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

package io.getstream.chat.android.models

/**
 * A content of message sync description.
 *
 * @see [MessageSyncDescription]
 */
public sealed class MessageSyncContent

/**
 * When no additional description is required to [SyncStatus].
 */
public object MessageSyncNone : MessageSyncContent() {
    override fun toString(): String = "MessageSyncNone"
}

/**
 * When sync is in progress.
 */
public sealed class MessageSyncInProgress : MessageSyncContent()

/**
 * When sync is failed.
 */
public sealed class MessageSyncFailed : MessageSyncContent()

/**
 * When sync is in progress due to awaiting attachments.
 */
public object MessageAwaitingAttachments : MessageSyncInProgress() {
    override fun toString(): String = "MessageAwaitingAttachments"
}

/**
 * When sync is failed due to moderation violation.
 */
public data class MessageModerationFailed(
    val violations: List<ModerationViolation>,
) : MessageSyncFailed()

/**
 * Moderation violation details.
 */
public data class ModerationViolation(
    val code: Int,
    val messages: List<String>,
)
