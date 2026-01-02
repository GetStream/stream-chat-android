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
import androidx.test.uiautomator.UiObject2

public fun UiObject2.isDisplayed(): Boolean {
    return this.visibleCenter.y > 0
}

public fun BySelector.isDisplayed(): Boolean {
    return this.findObjects().isNotEmpty()
}

public fun BySelector.isEnabled(): Boolean {
    return this.findObject().isEnabled
}

public fun BySelector.isChecked(): Boolean {
    return this.findObject().isChecked
}

public fun BySelector.scrollUpUntilDisplayed(scrolls: Int = 5): UiObject2 {
    var counter = scrolls
    while (!this.isDisplayed() && counter > 0) {
        device.swipeDown()
        --counter
    }
    return this.findObject()
}

public fun BySelector.scrollDownUntilDisplayed(scrolls: Int = 5): UiObject2 {
    var counter = scrolls
    while (!this.isDisplayed() && counter > 0) {
        device.swipeUp()
        --counter
    }
    return this.findObject()
}
