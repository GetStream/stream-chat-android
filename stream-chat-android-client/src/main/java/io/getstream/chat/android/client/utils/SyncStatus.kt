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
 
package io.getstream.chat.android.client.utils

/**
 * If the message has been sent to the servers.
 */
public enum class SyncStatus(public val status: Int) {
    /**
     * When the entity is new or changed.
     */
    SYNC_NEEDED(-1),

    /**
     * When the entity has been successfully synced.
     */
    COMPLETED(1),

    /**
     * After the retry strategy we still failed to sync this.
     */
    FAILED_PERMANENTLY(2),

    /**
     * When sync is in progress.
     */
    IN_PROGRESS(3),

    /**
     * When message waits its' attachments to be sent.
     */
    AWAITING_ATTACHMENTS(4);

    public companion object {
        private val map = values().associateBy(SyncStatus::status)
        public fun fromInt(type: Int): SyncStatus? = map[type]
    }
}
