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

package io.getstream.chat.android.compose.uiautomator

import androidx.test.uiautomator.BySelector
import androidx.test.uiautomator.StaleObjectException
import androidx.test.uiautomator.UiObject2
import androidx.test.uiautomator.Until

public fun sleep(timeOutMillis: Long = defaultTimeout) {
    Thread.sleep(timeOutMillis)
}

public fun BySelector.waitToAppear(timeOutMillis: Long = defaultTimeout): UiObject2 {
    wait(timeOutMillis)
    return findObject()
}

public fun BySelector.waitToAppear(withIndex: Int, timeOutMillis: Long = defaultTimeout): UiObject2 {
    wait(timeOutMillis)
    return findObjects()[withIndex]
}

public fun BySelector.wait(timeOutMillis: Long = defaultTimeout): BySelector {
    device.wait(Until.hasObject(this), timeOutMillis)
    return this
}

public fun BySelector.waitToDisappear(timeOutMillis: Long = defaultTimeout): BySelector {
    device.wait(Until.gone(this), timeOutMillis)
    return this
}

/**
 * Polls by re-finding the object on each iteration so a mid-poll recomposition does not produce
 * a [StaleObjectException]. Returns the text that was observed when the match succeeded, or the
 * last observed text on timeout — never throws. Callers typically wrap the result in an
 * assertion to surface mismatch/timeout.
 *
 * @param expectedText The text to match.
 * @param mustBeEqual When `true`, requires exact match; otherwise a substring match.
 * @param timeOutMillis Maximum time to keep polling before returning the last observed text.
 */
public fun BySelector.waitForText(
    expectedText: String,
    mustBeEqual: Boolean = true,
    timeOutMillis: Long = defaultTimeout,
): String {
    val endTime = System.currentTimeMillis() + timeOutMillis
    var lastText = ""
    while (System.currentTimeMillis() < endTime) {
        val actual = currentTextOrNull()
        if (actual != null) {
            lastText = actual
            val matches = if (mustBeEqual) actual == expectedText else actual.contains(expectedText)
            if (matches) return actual
        }
    }
    return lastText
}

// Call [device] directly — [findObject] lies about nullability and NPEs when the selector hasn't
// matched yet, which is the normal case during polling.
private fun BySelector.currentTextOrNull(): String? = try {
    device.findObject(this)?.text
} catch (_: StaleObjectException) {
    null
}

public fun BySelector.waitForCount(count: Int, timeOutMillis: Long = defaultTimeout): List<UiObject2> {
    val endTime = System.currentTimeMillis() + timeOutMillis
    var elements: List<UiObject2> = emptyList()
    var success = false
    while (!success && System.currentTimeMillis() < endTime) {
        elements = findObjects()
        success = elements.size == count
    }
    return elements
}

public fun UiObject2.waitForTextToChange(text: String, timeOutMillis: Long = defaultTimeout): UiObject2 {
    val endTime = System.currentTimeMillis() + timeOutMillis
    var textChanged = false
    while (!textChanged && System.currentTimeMillis() < endTime) {
        textChanged = this.text != text
    }
    return this
}
