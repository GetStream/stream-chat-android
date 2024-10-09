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

import android.app.Instrumentation
import android.content.Context
import android.content.res.AssetManager
import android.content.res.Resources
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.UiDevice

/**
 * Default timeout.
 */
val defaultTimeout: Long = 5.seconds

/**
 * UIDevice property initialized by singleton [UiDevice.getInstance] method, for the running Instrumentation.
 */
val device: UiDevice get() = UiDevice.getInstance(instrumentation)

/**
 * Application context.
 */
val appContext: Context get() = InstrumentationRegistry.getInstrumentation().targetContext

/**
 * Instrumentation context.
 */
val testContext: Context get() = InstrumentationRegistry.getInstrumentation().context

/**
 * AssetManager for androidTest environment.
 */
val testAssets: AssetManager = testContext.assets

/**
 * Test runner instrumentation.
 */
val instrumentation: Instrumentation get() = InstrumentationRegistry.getInstrumentation()

/**
 * Application resources.
 */
val resources: Resources get() = appContext.resources

/**
 * Application package name.
 */
val packageName: String get() = appContext.packageName.removeSuffix(".test")

/**
 * Get name of the resource by id.
 */
infix fun Resources.nameOf(viewId: Int): String = getResourceName(viewId)

/**
 * Get string value of the resource by id.
 */
infix fun Context.stringOf(stringResId: Int) = getString(stringResId)

/**
 * Execute adb shell command.
 */
fun execShell(command: String) {
    instrumentation.uiAutomation.executeShellCommand(command)
}
