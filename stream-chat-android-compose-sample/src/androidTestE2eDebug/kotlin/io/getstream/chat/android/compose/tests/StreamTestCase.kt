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

import android.Manifest.permission.POST_NOTIFICATIONS
import android.Manifest.permission.READ_EXTERNAL_STORAGE
import android.Manifest.permission.READ_MEDIA_IMAGES
import android.Manifest.permission.READ_MEDIA_VIDEO
import android.Manifest.permission.WRITE_EXTERNAL_STORAGE
import android.annotation.SuppressLint
import android.content.Intent
import io.getstream.chat.android.compose.robots.UserRobot
import io.getstream.chat.android.compose.sample.ui.InitTestActivity
import io.getstream.chat.android.compose.uiautomator.device
import io.getstream.chat.android.compose.uiautomator.grantPermission
import io.getstream.chat.android.compose.uiautomator.packageName
import io.getstream.chat.android.compose.uiautomator.testContext
import io.getstream.chat.android.e2e.test.mockserver.MockServer
import io.getstream.chat.android.e2e.test.robots.BackendRobot
import io.getstream.chat.android.e2e.test.robots.ParticipantRobot
import io.getstream.chat.android.e2e.test.rules.RetryRule
import io.qameta.allure.android.rules.LogcatRule
import io.qameta.allure.android.rules.ScreenshotRule
import io.qameta.allure.android.rules.WindowHierarchyRule
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.rules.TestName

abstract class StreamTestCase {

    lateinit var mockServer: MockServer
    val userRobot = UserRobot()
    lateinit var backendRobot: BackendRobot
    lateinit var participantRobot: ParticipantRobot

    @get:Rule
    var testName: TestName = TestName()

    @get:Rule
    val screenshotRule = ScreenshotRule(mode = ScreenshotRule.Mode.FAILURE, screenshotName = "screenshot")

    @get:Rule
    val logcatRule = LogcatRule()

    @get:Rule
    val windowHierarchyRule = WindowHierarchyRule()

    @get:Rule
    val retryRule = RetryRule()

    @Before
    fun setUp() {
        mockServer = MockServer(testName.methodName)
        backendRobot = BackendRobot(mockServer)
        participantRobot = ParticipantRobot(mockServer)
        startApp()
        grantAppPermissions()
    }

    @After
    fun tearDown() {
        mockServer.stop()
    }

    @SuppressLint("InlinedApi")
    private fun grantAppPermissions() {
        for (permission in arrayOf(
            POST_NOTIFICATIONS,
            READ_MEDIA_VIDEO,
            READ_MEDIA_IMAGES,
            READ_EXTERNAL_STORAGE,
            WRITE_EXTERNAL_STORAGE)
        ) {
            device.grantPermission(permission)
        }
    }

    abstract fun initTestActivity(): InitTestActivity

    private fun startApp() {
        testContext.packageManager.getLaunchIntentForPackage(packageName)?.let {
            it.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
            it.putExtra("BASE_URL", mockServer.url)
            it.putExtra("InitTestActivity", initTestActivity())
            testContext.startActivity(it)
        } ?: throw IllegalStateException("No launch intent found for package: $packageName")
    }
}
