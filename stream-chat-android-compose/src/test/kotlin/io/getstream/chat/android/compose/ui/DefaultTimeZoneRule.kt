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

package io.getstream.chat.android.compose.ui

import org.junit.rules.ExternalResource
import java.util.TimeZone

/**
 * Sets the default time zone to [timeZone] while a test runs, for the dates that `ChatTheme.dateFormatter` shows in
 * the device time zone.
 */
internal class DefaultTimeZoneRule(private val timeZone: TimeZone = TimeZone.getTimeZone("UTC")) : ExternalResource() {

    private lateinit var previous: TimeZone

    override fun before() {
        previous = TimeZone.getDefault()
        TimeZone.setDefault(timeZone)
    }

    override fun after() {
        TimeZone.setDefault(previous)
    }
}
