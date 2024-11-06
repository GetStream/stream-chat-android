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

import android.Manifest.permission
import android.Manifest.permission.POST_NOTIFICATIONS
import android.Manifest.permission.READ_MEDIA_IMAGES
import android.Manifest.permission.READ_MEDIA_VIDEO
import android.annotation.SuppressLint
import io.getstream.chat.android.compose.robots.UserRobot
import io.getstream.chat.android.compose.uiautomator.device
import io.getstream.chat.android.compose.uiautomator.grantPermission
import io.getstream.chat.android.compose.uiautomator.mockServer
import io.getstream.chat.android.compose.uiautomator.startApp
import io.getstream.chat.android.e2e.test.robots.ParticipantRobot
import org.junit.After
import org.junit.Before

open class StreamTestCase {

    val userRobot = UserRobot()
    val participantRobot = ParticipantRobot()

    @Before
    fun setUp() {
        mockServer.start()
        device.startApp()
        grantAppPermissions()
    }

    @After
    fun tearDown() {
        mockServer.stop()
    }

    @SuppressLint("InlinedApi")
    private fun grantAppPermissions() {
        for (permission in arrayOf(POST_NOTIFICATIONS, READ_MEDIA_VIDEO, READ_MEDIA_IMAGES)) {
            device.grantPermission(permission)
        }
    }
}
