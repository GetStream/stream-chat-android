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

package io.getstream.chat.android.ui.utils

import android.os.Build

/**
 * Checks whether the device manufacturer should consume long tap.
 * Fixes issue [#3255](https://github.com/GetStream/stream-chat-android/issues/3255)
 *
 * @return if manufacturer should consume long tap or not.
 */
internal fun shouldConsumeLongTap(): Boolean {
    val manufacturer = Build.MANUFACTURER.lowercase()
    return MANUFACTURERS_TO_CONSUME_LONG_TAP.any { it.lowercase() in manufacturer }
}

private const val MANUFACTURER_XIAOMI = "xiaomi"

/**
 * List of manufacturers which need to consume the long tap action.
 */
private val MANUFACTURERS_TO_CONSUME_LONG_TAP = listOf(
    MANUFACTURER_XIAOMI,
)
