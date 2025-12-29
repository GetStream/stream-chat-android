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

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import androidx.test.uiautomator.StaleObjectException
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.UiObject2
import io.qameta.allure.kotlin.Allure
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.File

public fun UiDevice.stopApp() {
    executeShellCommand("pm clear $packageName")
}

public fun UiDevice.typeText(text: String) {
    executeShellCommand("input text '$text'")
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

public fun UiDevice.swipeDown(times: Int = 1, steps: Int = 10) {
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

public fun UiDevice.swipeUp(times: Int = 1, steps: Int = 10) {
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
    waitForInternetConnection()
}

public fun UiDevice.disableInternetConnection() {
    executeShellCommand("svc data disable")
    executeShellCommand("svc wifi disable")
}

@SuppressLint("NewApi")
public fun UiDevice.waitForInternetConnection(timeoutMs: Long = 10000, intervalMs: Long = 500) {
    val connectivityManager = appContext.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

    val startTime = System.currentTimeMillis()
    while (System.currentTimeMillis() - startTime < timeoutMs) {
        val activeNetwork = connectivityManager.activeNetwork
        val capabilities = connectivityManager.getNetworkCapabilities(activeNetwork)
        val hasInternet = capabilities?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) == true

        if (hasInternet) return
        Thread.sleep(intervalMs)
    }

    throw IllegalStateException("There is no internet connection.")
}

public fun UiDevice.dumpWindowHierarchy(print: Boolean = true): String {
    val outputStream = ByteArrayOutputStream()
    device.dumpWindowHierarchy(outputStream)

    val outputString = outputStream.toString("UTF-8")
    if (print) {
        println(outputString)
    }
    return outputString
}

public fun <T> UiDevice.retryOnStaleObjectException(retries: Int = 3, action: () -> T): T {
    repeat(retries - 1) {
        try {
            return action()
        } catch (e: StaleObjectException) {
            println(e)
            Thread.sleep(500)
        }
    }
    return action()
}
public fun UiDevice.allureScreenshot(name: String) {
    val screenshot: Bitmap? = instrumentation.uiAutomation.takeScreenshot()

    screenshot?.let {
        val outputStream = ByteArrayOutputStream()
        it.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
        Allure.attachment(
            name = "$name.png",
            type = "image/png",
            fileExtension = ".png",
            content = ByteArrayInputStream(outputStream.toByteArray()),
        )
    }
}

public fun UiDevice.allureScreenrecord(name: String, file: File) {
    Allure.attachment(
        name = "$name.mp4",
        type = "video/mp4",
        fileExtension = ".mp4",
        content = file.inputStream(),
    )
}

public fun UiDevice.allureLogcat(name: String) {
    Allure.attachment(
        name = "$name.txt",
        type = "text/plain",
        fileExtension = ".txt",
        content = device.executeShellCommand("logcat -d"),
    )
}

public fun UiDevice.allureWindowHierarchy(name: String) {
    Allure.attachment(
        name = "$name.xml",
        type = "text/xml",
        fileExtension = ".xml",
        content = device.dumpWindowHierarchy(print = false),
    )
}
