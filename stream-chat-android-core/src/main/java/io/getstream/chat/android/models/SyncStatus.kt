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

private const val SYNC_NEEDED_STATUS_CODE = -1
private const val COMPLETED_STATUS_CODE = 1
private const val FAILED_PERMANENTLY_STATUS_CODE = 2
private const val IN_PROGRESS_STATUS_CODE = 3
private const val AWAITING_ATTACHMENTS_STATUS_CODE = 4

/**
 * If the message has been sent to the servers.
 *
 * @param status The numeric identifier of the status.
 */
public enum class SyncStatus(public val status: Int) {
    /**
     * When the entity is new or changed.
     */
    SYNC_NEEDED(SYNC_NEEDED_STATUS_CODE),

    /**
     * When the entity has been successfully synced.
     */
    COMPLETED(COMPLETED_STATUS_CODE),

    /**
     * After the retry strategy we still failed to sync this.
     */
    FAILED_PERMANENTLY(FAILED_PERMANENTLY_STATUS_CODE),

    /**
     * When sync is in progress.
     */
    IN_PROGRESS(IN_PROGRESS_STATUS_CODE),

    /**
     * When message waits its' attachments to be sent.
     */
    AWAITING_ATTACHMENTS(AWAITING_ATTACHMENTS_STATUS_CODE),
    ;

    public companion object {
        private val map = entries.associateBy(SyncStatus::status)

        /**
         * Get the [SyncStatus] from the given [type].
         */
        public fun fromInt(type: Int): SyncStatus? = map[type]
    }
}
