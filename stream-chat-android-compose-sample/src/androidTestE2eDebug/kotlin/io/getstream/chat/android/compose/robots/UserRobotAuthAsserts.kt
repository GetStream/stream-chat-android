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

package io.getstream.chat.android.compose.robots

import io.getstream.chat.android.compose.pages.JwtPage
import io.getstream.chat.android.compose.uiautomator.waitForText
import io.getstream.chat.android.compose.uiautomator.waitToAppear
import io.getstream.chat.android.models.ConnectionState
import io.getstream.chat.android.models.InitializationState
import org.junit.Assert.assertEquals

fun UserRobot.assertConnectionStatus(status: ConnectionState): UserRobot {
    assertEquals(status.toString(), JwtPage.connectionStatus.waitToAppear().waitForText(status.toString()).text)
    return this
}

fun UserRobot.assertInitializationStatus(status: InitializationState): UserRobot {
    assertEquals(status.toString(), JwtPage.initializationStatus.waitToAppear().waitForText(status.toString()).text)
    return this
}
