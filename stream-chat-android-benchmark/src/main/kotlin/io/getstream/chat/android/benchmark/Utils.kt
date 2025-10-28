/*
 * Copyright (c) 2014-2023 Stream.io Inc. All rights reserved.
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

package io.getstream.chat.android.benchmark

import androidx.test.uiautomator.BySelector
import androidx.test.uiautomator.Direction
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.UiObject2
import androidx.test.uiautomator.Until

internal const val COMPOSE_SAMPLE_PACKAGE_NAME = "io.getstream.chat.android.compose.sample"

internal const val FLING_SCREEN_DIVIDER = 5

internal const val STANDARD_TIMEOUT = 15_000L

internal fun UiDevice.flingElementDownUp(element: UiObject2) {
    // Set some margin from the sides to prevent triggering system navigation
    element.setGestureMargin(displayWidth / FLING_SCREEN_DIVIDER)

    element.fling(Direction.DOWN)
    waitForIdle()
    element.fling(Direction.UP)
}

internal fun UiDevice.flingElementUpDown(element: UiObject2) {
    // Set some margin from the sides to prevent triggering system navigation
    element.setGestureMargin(displayWidth / FLING_SCREEN_DIVIDER)

    element.fling(Direction.UP)
    waitForIdle()
    element.fling(Direction.DOWN)
}

/**
 * Waits until an object with [selector] if visible on screen and returns the object.
 * If the element is not available in [timeout], throws [AssertionError]
 */
internal fun UiDevice.waitAndFindObject(
    selector: BySelector,
    timeout: Long = STANDARD_TIMEOUT,
): UiObject2 {
    if (!wait(Until.hasObject(selector), timeout)) {
        throw AssertionError("Element not found on screen in ${timeout}ms (selector=$selector)")
    }

    return findObject(selector)
}

internal fun UiDevice.waitForObject(
    selector: BySelector,
    timeout: Long = 5_000,
): UiObject2? {
    if (wait(Until.hasObject(selector), timeout)) {
        return findObject(selector)
    }
    return null
}
