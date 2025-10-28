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

package io.getstream.chat.android.test

import io.getstream.log.KotlinStreamLogger
import io.getstream.log.Priority
import io.getstream.log.StreamLog
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

public object TestLoggingHelper {
    public fun initialize() {
        StreamLog.setValidator { _, _ -> true }
        StreamLog.install(StreamTestLogger())
    }
}

internal class StreamTestLogger : KotlinStreamLogger() {
    override fun log(
        priority: Priority,
        tag: String,
        message: String,
        throwable: Throwable?,
    ) {
        // No-Op
    }

    override val now: () -> LocalDateTime
        get() = { Clock.System.now().toLocalDateTime(TimeZone.UTC) }

    override fun install(
        minPriority: Priority,
        maxTagLength: Int,
    ) {
        // No-Op
    }
}
