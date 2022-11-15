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

private const val NONE_TYPE = 0
private const val IN_PROGRESS_AWAIT_ATTACHMENTS_TYPE = 100
private const val FAILED_MODERATION_TYPE = 200

/**
 * A type of message sync description.
 *
 * @param alias An string representation of the [MessageSyncType].
 * @param type An integer representation of the [MessageSyncType].
 *
 * @see [MessageSyncDescription]
 */
public enum class MessageSyncType(
    public val alias: String,
    public val type: Int,
) {

    /**
     * When no additional description is required to [SyncStatus].
     */
    NONE(
        alias = "message.sync.none",
        type = NONE_TYPE
    ),

    /**
     * When sync is in progress due to awaiting attachments.
     *
     * @see [SyncStatus.IN_PROGRESS]
     */
    IN_PROGRESS_AWAIT_ATTACHMENTS(
        alias = "message.sync.in_progress.await_attachments",
        type = IN_PROGRESS_AWAIT_ATTACHMENTS_TYPE
    ),

    /**
     * Failed due to moderation violation.
     *
     * @see [SyncStatus.FAILED_PERMANENTLY]
     */
    FAILED_MODERATION(
        alias = "message.sync.failed.moderation",
        type = FAILED_MODERATION_TYPE
    );

    public companion object {
        private val map = MessageSyncType.values().associateBy(MessageSyncType::type)
        public fun fromInt(type: Int): MessageSyncType? = map[type]
        public const val TYPE: String = "type"
    }
}
