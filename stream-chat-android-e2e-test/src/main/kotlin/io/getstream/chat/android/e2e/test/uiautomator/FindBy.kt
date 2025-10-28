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

import android.view.View
import androidx.test.uiautomator.By
import androidx.test.uiautomator.BySelector
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.UiObject2
import kotlin.reflect.KClass

/**
 * BySelector extension function, which is getting the UIObject.
 */
public fun BySelector.findObject(): UiObject2 = device.findObject(this)

/**
 * BySelector extension function, which is getting the UIObject.
 */
public fun BySelector.findObjects(): List<UiObject2> = device.findObjects(this)

/**
 * UIDevice extension function, which is getting the UIObject by selecting the component with text value.
 */
public infix fun UiDevice.findObjectByText(text: String): UiObject2 = findObject(By.text(text))

/**
 * UIDevice extension function, which is getting the UIObject by selecting the component that contains text value.
 */
public infix fun UiDevice.findObjectByTextContains(text: String): UiObject2 = findObject(By.textContains(text))

/**
 * UIDevice extension function, which is getting the UIObject by selecting the component with [KClass] type.
 */
public infix fun <K : View> UiDevice.findObjectByType(kClass: () -> KClass<K>): UiObject2 = findObject(By.clazz(kClass().java))
