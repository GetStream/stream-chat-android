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

package io.getstream.chat.android.compose.tests

import io.getstream.chat.android.compose.robots.assertDeletedMessage
import io.getstream.chat.android.compose.robots.assertFile
import io.getstream.chat.android.compose.robots.assertFileAttachmentInPreview
import io.getstream.chat.android.compose.robots.assertImage
import io.getstream.chat.android.compose.robots.assertMediaAttachmentInPreview
import io.getstream.chat.android.compose.robots.assertVideo
import io.getstream.chat.android.compose.sample.ui.InitTestActivity
import io.getstream.chat.android.e2e.test.mockserver.AttachmentType
import io.qameta.allure.kotlin.Allure.step
import io.qameta.allure.kotlin.AllureId
import org.junit.Test

class AttachmentsTests : StreamTestCase() {

    override fun initTestActivity() = InitTestActivity.UserLogin

    @AllureId("5663")
    @Test
    fun test_uploadImage() {
        step("GIVEN user opens the channel") {
            userRobot.login().openChannel()
        }
        step("WHEN user attaches an image") {
            userRobot.uploadAttachment(type = AttachmentType.IMAGE, send = false)
        }
        step("THEN image is displayed in preview") {
            userRobot.assertMediaAttachmentInPreview(isDisplayed = true)
        }
        step("WHEN user sends an image") {
            userRobot.tapOnSendButton()
        }
        step("THEN user can see uploaded image") {
            userRobot.assertImage(isDisplayed = true)
        }
    }

    @AllureId("6824")
    @Test
    fun test_uploadMultipleImages() {
        step("GIVEN user opens the channel") {
            userRobot.login().openChannel()
        }
        step("WHEN user attaches multiple images") {
            userRobot.uploadAttachment(type = AttachmentType.IMAGE, multiple = true, send = false)
        }
        step("THEN images are displayed in preview") {
            userRobot.assertMediaAttachmentInPreview(isDisplayed = true, count = 2)
        }
        step("WHEN user sends the images") {
            userRobot.tapOnSendButton()
        }
        step("THEN user can see uploaded images") {
            userRobot.assertImage(isDisplayed = true, count = 2)
        }
    }

    @AllureId("6825")
    @Test
    fun test_deleteImage() {
        step("GIVEN user opens the channel") {
            userRobot.login().openChannel()
        }
        step("WHEN user sends an image") {
            userRobot.uploadAttachment(type = AttachmentType.IMAGE)
        }
        step("AND user deletes an image") {
            userRobot.deleteMessage()
        }
        step("THEN user can see deleted message") {
            userRobot
                .assertImage(isDisplayed = false)
                .assertDeletedMessage()
        }
    }

    @AllureId("6826")
    @Test
    fun test_uploadFile() {
        step("GIVEN user opens the channel") {
            userRobot.login().openChannel()
        }
        step("WHEN user sends a file") {
            userRobot.uploadAttachment(type = AttachmentType.FILE, send = false)
        }
        step("THEN file is displayed in preview") {
            userRobot.assertFileAttachmentInPreview(isDisplayed = true)
        }
        step("WHEN user sends a file") {
            userRobot.tapOnSendButton()
        }
        step("THEN user can see uploaded file") {
            userRobot.assertFile(isDisplayed = true)
        }
    }

    @AllureId("6827")
    @Test
    fun test_uploadMultipleFiles() {
        step("GIVEN user opens the channel") {
            userRobot.login().openChannel()
        }
        step("WHEN user attaches multiple files") {
            userRobot.uploadAttachment(type = AttachmentType.FILE, multiple = true, send = false)
        }
        step("THEN files are displayed in preview") {
            userRobot.assertFileAttachmentInPreview(isDisplayed = true, count = 2)
        }
        step("WHEN user sends the files") {
            userRobot.tapOnSendButton()
        }
        step("THEN user can see uploaded files") {
            userRobot.assertFile(isDisplayed = true, count = 2)
        }
    }

    @AllureId("6828")
    @Test
    fun test_deleteFile() {
        step("GIVEN user opens the channel") {
            userRobot.login().openChannel()
        }
        step("WHEN user sends a file") {
            userRobot.uploadAttachment(type = AttachmentType.IMAGE)
        }
        step("AND user deletes a file") {
            userRobot.deleteMessage()
        }
        step("THEN user can see deleted message") {
            userRobot
                .assertImage(isDisplayed = false)
                .assertDeletedMessage()
        }
    }

    @AllureId("5664")
    @Test
    fun test_participantUploadsImage() {
        step("GIVEN user opens the channel") {
            userRobot.login().openChannel()
        }
        step("WHEN participant uploads an image") {
            participantRobot.uploadAttachment(type = AttachmentType.IMAGE)
        }
        step("THEN user can see uploaded image") {
            userRobot.assertImage(isDisplayed = true)
        }
    }

    @AllureId("5666")
    @Test
    fun test_participantUploadsVideo() {
        step("GIVEN user opens the channel") {
            userRobot.login().openChannel()
        }
        step("WHEN participant uploads a video") {
            participantRobot.uploadAttachment(type = AttachmentType.VIDEO)
        }
        step("THEN user can see uploaded video") {
            userRobot.assertVideo(isDisplayed = true)
        }
    }

    @AllureId("6829")
    @Test
    fun test_participantUploadsFile() {
        step("GIVEN user opens the channel") {
            userRobot.login().openChannel()
        }
        step("WHEN participant uploads a file") {
            participantRobot.uploadAttachment(type = AttachmentType.FILE)
        }
        step("THEN user can see uploaded file") {
            userRobot.assertFile(isDisplayed = true)
        }
    }
}
