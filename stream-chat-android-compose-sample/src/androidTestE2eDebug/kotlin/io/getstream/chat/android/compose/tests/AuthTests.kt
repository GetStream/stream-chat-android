/*
 * Copyright (c) 2014-2025 Stream.io Inc. All rights reserved.
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

import io.getstream.chat.android.compose.pages.JwtPage
import io.getstream.chat.android.compose.robots.UserRobot
import io.getstream.chat.android.compose.sample.ui.InitTestActivity
import io.getstream.chat.android.compose.uiautomator.device
import io.getstream.chat.android.compose.uiautomator.disableInternetConnection
import io.getstream.chat.android.compose.uiautomator.enableInternetConnection
import io.getstream.chat.android.compose.uiautomator.goToBackground
import io.getstream.chat.android.compose.uiautomator.goToForeground
import io.getstream.chat.android.compose.uiautomator.isDisplayed
import io.getstream.chat.android.compose.uiautomator.seconds
import io.getstream.chat.android.compose.uiautomator.waitToAppear
import io.qameta.allure.kotlin.Allure.step
import io.qameta.allure.kotlin.AllureId
import org.junit.Assert.assertTrue
import org.junit.Test

// In order to run the authentication e2e tests,
// Make sure you've set up a `STREAM_DEMO_APP_SECRET` env var
// With a secret from: https://dashboard.getstream.io/app/102399/chat/

class AuthTests : StreamTestCase() {

    override fun initTestActivity() = InitTestActivity.Jwt(baseUrl = mockServer.url)

    @AllureId("6857")
    @Test
    fun test_tokenInvalidatesBeforeUserLogsIn() {
        step("GIVEN token is invalid") {
            backendRobot.invalidateToken()
        }
        step("WHEN user tries to log in") {
            userRobot.tapOnConnectionButton()
        }
        step("THEN app requests a token refresh") {
            userRobot.assertConnectionStatus()
        }
    }

    @AllureId("6858")
    @Test
    fun test_tokenInvalidatesAfterUserLogsIn() {
        step("GIVEN user logs in") {
            userRobot
                .tapOnConnectionButton()
                .assertConnectionStatus()
        }
        step("WHEN token invalidates") {
            backendRobot.invalidateToken()
        }
        step("THEN app requests a token refresh") {
            userRobot
                .assertTokenHasExpired()
                .assertConnectionStatus()
        }
    }

    @AllureId("6859")
    @Test
    fun test_tokenDateInvalidatesBeforeUserLogsIn() {
        step("GIVEN token is invalid") {
            backendRobot.invalidateTokenDate()
        }
        step("WHEN user tries to log in") {
            userRobot.tapOnConnectionButton()
        }
        step("THEN app requests a token refresh") {
            userRobot.assertConnectionStatus()
        }
    }

    @AllureId("6860")
    @Test
    fun test_tokenDateInvalidatesAfterUserLogsIn() {
        step("GIVEN user logs in") {
            userRobot
                .tapOnConnectionButton()
                .assertConnectionStatus()
        }
        step("WHEN token invalidates") {
            backendRobot.invalidateTokenDate()
        }
        step("THEN app requests a token refresh") {
            userRobot
                .assertTokenHasExpired()
                .assertConnectionStatus()
        }
    }

    @AllureId("6861")
    @Test
    fun test_tokenSignatureInvalidatesBeforeUserLogsIn() {
        step("GIVEN token is invalid") {
            backendRobot.invalidateTokenSignature()
        }
        step("WHEN user tries to log in") {
            userRobot.tapOnConnectionButton()
        }
        step("THEN app requests a token refresh") {
            userRobot.assertConnectionStatus()
        }
    }

    @AllureId("6862")
    @Test
    fun test_tokenSignatureInvalidatesAfterUserLogsIn() {
        step("GIVEN user logs in") {
            userRobot
                .tapOnConnectionButton()
                .assertConnectionStatus()
        }
        step("WHEN token invalidates") {
            backendRobot.invalidateTokenSignature()
        }
        step("THEN app requests a token refresh") {
            userRobot
                .assertTokenHasExpired()
                .assertConnectionStatus()
        }
    }

    @AllureId("5849")
    @Test
    fun test_tokenExpiresBeforeUserLogsIn() {
        step("GIVEN token expires") {
            backendRobot.revokeToken()
        }
        step("WHEN user tries to log in") {
            userRobot.tapOnConnectionButton()
        }
        step("THEN app requests a token refresh") {
            userRobot.assertConnectionStatus()
        }
    }

    @AllureId("5850")
    @Test
    fun test_tokenExpiresAfterUserLoggedIn() {
        step("GIVEN user logs in") {
            userRobot
                .tapOnConnectionButton()
                .assertConnectionStatus()
        }
        step("WHEN token expires") {
            // No explicit action, expected system behavior
        }
        step("THEN app requests a token refresh") {
            userRobot
                .assertTokenHasExpired()
                .assertConnectionStatus()
        }
    }

    @AllureId("5851")
    @Test
    fun test_tokenExpiresWhenUserIsInBackground() {
        step("GIVEN user logs in") {
            userRobot
                .tapOnConnectionButton()
                .assertConnectionStatus()
        }
        step("AND user goes to background") {
            device.goToBackground()
        }
        step("AND token expires") {
            userRobot.waitForTokenToExpire()
        }
        step("WHEN user comes back to foreground") {
            device.goToForeground()
        }
        step("THEN app requests a token refresh") {
            userRobot
                .assertTokenHasExpired()
                .assertConnectionStatus()
        }
    }

    @AllureId("5852")
    @Test
    fun test_tokenExpiresWhileUserIsOffline() {
        step("GIVEN user logs in") {
            userRobot
                .tapOnConnectionButton()
                .assertConnectionStatus()
        }
        step("AND user goes offline") {
            device.disableInternetConnection()
        }
        step("WHEN token expires") {
            userRobot.waitForTokenToExpire()
        }
        step("WHEN user comes back online") {
            device.enableInternetConnection()
        }
        step("THEN app requests a token refresh") {
            userRobot
                .assertTokenHasExpired()
                .assertConnectionStatus()
        }
    }

    @AllureId("5853")
    @Test
    fun test_tokenGenerationFails() {
        step("GIVEN user logs in") {
            userRobot
                .tapOnConnectionButton()
                .assertConnectionStatus()
        }
        step("AND JWT generation breaks on server side") {
            backendRobot.breakTokenGeneration()
        }
        step("WHEN app tries to request a token refresh") {
            userRobot.assertTokenHasExpired()
        }
        step("THEN server returns an error (500)") {
            // No explicit action, expected system behavior
        }
        step("WHEN JWT generation recovers on server side") {
            // No explicit action, expected system behavior
        }
        step("THEN app requests a token refresh again") {
            userRobot.assertConnectionStatus()
        }
    }

    private fun UserRobot.assertConnectionStatus(): UserRobot {
        assertTrue(JwtPage.statusConnected.waitToAppear(15.seconds).isDisplayed())
        return this
    }

    private fun UserRobot.assertTokenHasExpired(): UserRobot {
        assertTrue(JwtPage.statusOffline.waitToAppear(15.seconds).isDisplayed())
        return this
    }

    private fun UserRobot.tapOnConnectionButton(): UserRobot {
        JwtPage.connectionButton.waitToAppear().click()
        return this
    }

    private fun UserRobot.waitForTokenToExpire(): UserRobot {
        sleep(5.seconds)
        return this
    }
}
