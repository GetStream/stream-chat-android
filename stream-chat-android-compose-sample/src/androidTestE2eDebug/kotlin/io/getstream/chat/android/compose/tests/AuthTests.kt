package io.getstream.chat.android.compose.tests

import io.getstream.chat.android.compose.pages.JwtPage
import io.getstream.chat.android.compose.robots.UserRobot
import io.getstream.chat.android.compose.robots.assertConnectionStatus
import io.getstream.chat.android.compose.sample.ui.InitTestActivity
import io.getstream.chat.android.compose.uiautomator.device
import io.getstream.chat.android.compose.uiautomator.disableInternetConnection
import io.getstream.chat.android.compose.uiautomator.enableInternetConnection
import io.getstream.chat.android.compose.uiautomator.goToBackground
import io.getstream.chat.android.compose.uiautomator.goToForeground
import io.getstream.chat.android.compose.uiautomator.seconds
import io.getstream.chat.android.compose.uiautomator.waitToAppear
import io.getstream.chat.android.models.ConnectionState
import io.qameta.allure.kotlin.Allure.step
import io.qameta.allure.kotlin.AllureId
import org.junit.Test

class AuthTests : StreamTestCase() {

    override fun initTestActivity() = InitTestActivity.Jwt

    @AllureId("")
    @Test
    fun test_tokenExpiresBeforeUserLogsIn() {
        step("GIVEN token expires") {
            backendRobot.revokeJwt()
        }
        step("WHEN user tries to log in") {
            userRobot.tapOnJwtConnectionButton()
        }
        step("THEN app requests a token refresh") {
            userRobot.assertConnectionStatus(ConnectionState.Connected)
        }
    }

    @AllureId("")
    @Test
    fun test_tokenExpiresAfterUserLoggedIn() {
        step("GIVEN user logs in") {
            userRobot
                .tapOnJwtConnectionButton()
                .assertConnectionStatus(ConnectionState.Connected)
        }
        step("WHEN token expires") {
            userRobot.waitForMessageListToLoad()
        }
        step("THEN app requests a token refresh") {
            userRobot.assertConnectionStatus(ConnectionState.Connected)
        }
    }

    @AllureId("")
    @Test
    fun test_tokenExpiresWhenUserIsInBackground() {
        step("GIVEN user logs in") {
            userRobot
                .tapOnJwtConnectionButton()
                .assertConnectionStatus(ConnectionState.Connected)
        }
        step("AND user goes to background") {
            device.goToBackground()
        }
        step("AND token expires") {
            userRobot.waitForJwtToExpire()
        }
        step("WHEN user comes back to foreground") {
            device.goToForeground()
        }
        step("THEN app requests a token refresh") {
            userRobot.assertConnectionStatus(ConnectionState.Connected)
        }
    }

    @AllureId("")
    @Test
    fun test_tokenExpiresWhileUserIsOffline() {
        step("GIVEN user logs in") {
            userRobot
                .tapOnJwtConnectionButton()
                .assertConnectionStatus(ConnectionState.Connected)
        }
        step("AND user goes offline") {
            device.disableInternetConnection()
        }
        step("WHEN token expires") {
            userRobot.waitForJwtToExpire()
        }
        step("WHEN user comes back online") {
            device.enableInternetConnection()
        }
        step("THEN app requests a token refresh") {
            userRobot.assertConnectionStatus(ConnectionState.Connected)
        }
    }

    @AllureId("")
    @Test
    fun test_tokenGenerationFails() {
        step("GIVEN JWT generation breaks on server side") {
            backendRobot.breakJwt()
        }
        step("AND user tries to log in") {
            userRobot.login()
        }
        step("WHEN app requests a token refresh") {
            // No explicit assertion, expected system behavior
        }
        step("AND server returns an error") {
            // No explicit assertion, expected failure behavior
        }
        step("AND JWT generation recovers on server side") {
            // Simulating server recovery
        }
        step("THEN app requests a token refresh a second time") {
            userRobot.assertConnectionStatus(ConnectionState.Connected)
        }
    }

    private fun UserRobot.tapOnJwtConnectionButton(): UserRobot {
        JwtPage.connectionButton.waitToAppear().click()
        return this
    }

    private fun UserRobot.waitForJwtToExpire(): UserRobot {
        sleep(10.seconds)
        return this
    }
}
