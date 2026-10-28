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

package io.getstream.chat.android.models

import androidx.compose.runtime.Immutable

/**
 * A status the scheduled messages query can filter by. More statuses can be added in a minor version, so a `when` on
 * it needs an `else` branch.
 */
@Immutable
public abstract class ScheduledMessageQueryStatus internal constructor() {

    /**
     * The raw value of the status.
     */
    public abstract val value: String

    /**
     * The scheduled messages the server sends at their date. See [ScheduledMessageStatus.Scheduled].
     */
    public data object Scheduled : ScheduledMessageQueryStatus() {
        override val value: String = "scheduled"
    }

    /**
     * The scheduled messages the server is sending. See [ScheduledMessageStatus.Sending].
     */
    public data object Sending : ScheduledMessageQueryStatus() {
        override val value: String = "sending"
    }

    /**
     * The scheduled messages the server could not send. See [ScheduledMessageStatus.Failed].
     */
    public data object Failed : ScheduledMessageQueryStatus() {
        override val value: String = "failed"
    }
}
