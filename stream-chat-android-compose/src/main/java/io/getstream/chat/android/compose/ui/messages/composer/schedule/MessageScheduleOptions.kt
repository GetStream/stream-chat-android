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

package io.getstream.chat.android.compose.ui.messages.composer.schedule

import androidx.compose.runtime.staticCompositionLocalOf

/**
 * What the screen around the composer adds to scheduling, like it does to sending.
 *
 * @param skipPushNotification Whether the scheduled message skips the push notification when it is sent.
 * @param skipEnrichUrl Whether the scheduled message skips enriching its URLs.
 * @param onMessageScheduled Called once the content of the composer is scheduled, so the screen can reset what it
 * keeps about it, like the selection of the attachment picker.
 */
internal class MessageScheduleOptions(
    val skipPushNotification: Boolean = false,
    val skipEnrichUrl: Boolean = false,
    val onMessageScheduled: () -> Unit = {},
)

internal val LocalMessageScheduleOptions = staticCompositionLocalOf { MessageScheduleOptions() }
