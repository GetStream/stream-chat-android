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

package io.getstream.chat.android.compose.tests

import io.getstream.chat.android.compose.pages.ChannelListPage
import io.getstream.chat.android.compose.pages.LoginPage
import io.getstream.chat.android.compose.uiautomator.device
import io.getstream.chat.android.compose.uiautomator.swipeUp
import io.getstream.chat.android.compose.uiautomator.typeText
import io.getstream.chat.android.compose.uiautomator.sleep
import io.getstream.chat.android.compose.uiautomator.startApp
import io.getstream.chat.android.compose.uiautomator.waitToAppear
import io.qameta.allure.kotlin.Allure.step
import org.junit.Assert.assertTrue
import org.junit.Test

class SampleTest : StreamTestCase() {

    @Test
    fun testSample() {
        step("Sample step") {
            LoginPage.loginButton.waitToAppear().click()
            ChannelListPage.searchField.waitToAppear().typeText("test")
            device.swipeUp()
            sleep()
            assertTrue(true)
        }
    }
}
