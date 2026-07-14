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

package io.getstream.chat.android.e2e.test.uiautomator

import androidx.test.uiautomator.BySelector
import androidx.test.uiautomator.StaleObjectException
import androidx.test.uiautomator.UiObject2

/**
 * Waits for objects matching this selector and returns them sorted by visual position,
 * bottom-most first. Message cells are enumerated in screen-reader reading order
 * (oldest-first), so lookups that mean "index 0 = the newest message at the visual bottom"
 * sort by on-screen position instead of relying on the enumeration order.
 *
 * Stale reads during the lookup are absorbed and retried until the timeout;
 * [StaleObjectException] never escapes.
 *
 * @param timeOutMillis Maximum time to wait before returning an empty list.
 */
public fun BySelector.waitToAppearBottomUp(timeOutMillis: Long = defaultTimeout): List<UiObject2> {
    val endTime = System.currentTimeMillis() + timeOutMillis
    while (System.currentTimeMillis() < endTime) {
        try {
            val objects = device.findObjects(this)
            if (objects.isNotEmpty()) {
                return objects.sortedByDescending { it.visibleBounds.top }
            }
        } catch (_: StaleObjectException) {
        }
        Thread.sleep(POLL_INTERVAL_MILLIS)
    }
    return emptyList()
}

/**
 * Waits for objects matching this selector and returns the one at [withIndex], counting
 * bottom-up (index 0 = the bottom-most object on screen).
 *
 * @param withIndex The zero-based index counting from the bottom-most object.
 * @param timeOutMillis Maximum time to wait before failing.
 * @throws IllegalStateException when fewer than [withIndex] + 1 objects match.
 */
public fun BySelector.waitToAppearBottomUp(withIndex: Int, timeOutMillis: Long = defaultTimeout): UiObject2 {
    val objects = waitToAppearBottomUp(timeOutMillis)
    return objects.getOrNull(withIndex)
        ?: error("waitToAppearBottomUp(withIndex=$withIndex): only ${objects.size} objects matched selector: $this")
}
