/*
 * Copyright (c) 2014-2024 Stream.io Inc. All rights reserved.
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

import android.content.Intent
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.UiObject2
import io.getstream.chat.android.e2e.test.mockserver.mockServerUrl

public fun UiDevice.startApp() {
    val intent = testContext.packageManager.getLaunchIntentForPackage(packageName)
    intent?.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
    intent?.putExtra("BASE_URL", mockServerUrl)
    testContext.startActivity(intent)
}

public fun UiDevice.stopApp() {
    executeShellCommand("pm clear $packageName")
}

public fun UiObject2.typeText(text: String): UiObject2 {
    this.text = text
    return this
}

public fun UiObject2.longPress(steps: Int = 100) {
    val centerX = this.visibleBounds.centerX()
    val centerY = this.visibleBounds.centerY()
    device.swipe(centerX, centerY, centerX, centerY, steps)
}

public fun UiDevice.swipeDown(steps: Int = 10, times: Int = 1) {
    repeat(times) {
        val middleOfTheScreenHorizontally = displayWidth / 2
        val middleOfTheScreenVertically = displayHeight / 2
        swipe(
            middleOfTheScreenHorizontally,
            middleOfTheScreenVertically,
            middleOfTheScreenHorizontally,
            displayHeight,
            steps,
        )
    }
}

public fun UiDevice.swipeUp(steps: Int = 10, times: Int = 1) {
    repeat(times) {
        val middleOfTheScreenHorizontally = displayWidth / 2
        val middleOfTheScreenVertically = displayHeight / 2
        swipe(
            middleOfTheScreenHorizontally,
            middleOfTheScreenVertically,
            middleOfTheScreenHorizontally,
            0,
            steps,
        )
    }
}

public fun UiDevice.tapOnScreenCenter() {
    device.click(device.displayWidth / 2, device.displayHeight / 2)
}

public fun UiDevice.goToBackground() {
    device.pressHome()
    sleep(1000)
}

public fun UiDevice.goToForeground() {
    device.pressRecentApps()
    sleep(500)
    device.tapOnScreenCenter()
}

public fun UiDevice.enableInternetConnection() {
    executeShellCommand("svc data enable")
    executeShellCommand("svc wifi enable")
}

public fun UiDevice.disableInternetConnection() {
    executeShellCommand("svc data disable")
    executeShellCommand("svc wifi disable")
}
