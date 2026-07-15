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

package io.getstream.chat.android.compose.robots

import androidx.test.uiautomator.By
import io.getstream.chat.android.e2e.test.uiautomator.device
import io.getstream.chat.android.e2e.test.uiautomator.isDisplayed
import io.getstream.chat.android.e2e.test.uiautomator.waitDisplayed
import io.getstream.chat.android.e2e.test.uiautomator.waitToDisappear
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue

/**
 * Opens the notification shade and asserts a notification with [text] is shown.
 * The shade stays open so the notification can be tapped next.
 */
fun UserRobot.assertPushNotification(text: String): UserRobot {
    openNotificationShade()
    assertTrue(By.text(text).waitDisplayed())
    return this
}

/**
 * Opens the notification shade, asserts no notification with [text] is shown, and closes it.
 */
fun UserRobot.assertPushNotificationDoesNotAppear(text: String): UserRobot {
    openNotificationShade()
    assertFalse(By.text(text).waitToDisappear().isDisplayed())
    device.pressBack()
    return this
}
