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

import io.getstream.chat.android.compose.robots.assertLinkPreviewInComposer
import io.getstream.chat.android.compose.robots.assertLinkPreviewInMessageList
import io.getstream.chat.android.compose.robots.assertMessage
import io.getstream.chat.android.compose.sample.ui.InitTestActivity
import io.qameta.allure.kotlin.Allure.step
import io.qameta.allure.kotlin.AllureId
import org.junit.Test

class HyperLinksTests : StreamTestCase() {

    override fun initTestActivity() = InitTestActivity.UserLogin
    private val youtubeVideoLink = "Look at https://youtube.com/watch?v=xOX7MsrbaPY"
    private val unsplashImageLink = "Look at https://unsplash.com/photos/1_2d3MRbI9c"
    private val giphyGifLink = "Look at https://giphy.com/gifs/test-gw3IWyGkC0rsazTi"

    @AllureId("5691")
    @Test
    fun test_unsplashLinkPreview() {
        step("GIVEN user opens the channel") {
            userRobot.login().openChannel()
        }
        step("WHEN user types an unsplash url") {
            userRobot.typeText(unsplashImageLink)
        }
        step("THEN user observes a link preview") {
            userRobot.assertLinkPreviewInComposer(isDisplayed = true)
        }
        step("WHEN user taps on the send button") {
            userRobot.tapOnSendButton()
        }
        step("THEN user observes a message with link preview") {
            userRobot
                .assertMessage(unsplashImageLink, isClickable = true)
                .assertLinkPreviewInMessageList(isDisplayed = true)
        }
    }

    @AllureId("6830")
    @Test
    fun test_unsplashLinkWithoutPreview() {
        step("GIVEN user opens the channel") {
            userRobot.login().openChannel()
        }
        step("WHEN user types an unsplash url") {
            userRobot.typeText(unsplashImageLink)
        }
        step("AND user cancels the link preview") {
            userRobot.tapOnAttachmentCancelIcon()
        }
        step("THEN link preview disappears") {
            userRobot.assertLinkPreviewInComposer(isDisplayed = false)
        }
        step("WHEN user taps on the send button") {
            userRobot.tapOnSendButton()
        }
        step("THEN user observes a message without link preview") {
            userRobot
                .assertMessage(unsplashImageLink, isClickable = true)
                .assertLinkPreviewInMessageList(isDisplayed = false)
        }
    }

    @AllureId("5692")
    @Test
    fun test_youtubeLinkPreview() {
        step("GIVEN user opens the channel") {
            userRobot.login().openChannel()
        }
        step("WHEN user types a youtube url") {
            userRobot.typeText(youtubeVideoLink)
        }
        step("THEN user observes a link preview") {
            userRobot.assertLinkPreviewInComposer(isDisplayed = true)
        }
        step("WHEN user taps on the send button") {
            userRobot.tapOnSendButton()
        }
        step("THEN user observes a message with link preview") {
            userRobot
                .assertMessage(youtubeVideoLink, isClickable = true)
                .assertLinkPreviewInMessageList(isDisplayed = true)
        }
    }

    @AllureId("6831")
    @Test
    fun test_youtubeLinkWithoutPreview() {
        step("GIVEN user opens the channel") {
            userRobot.login().openChannel()
        }
        step("WHEN user types a youtube url") {
            userRobot.typeText(youtubeVideoLink)
        }
        step("AND user cancels the link preview") {
            userRobot.tapOnAttachmentCancelIcon()
        }
        step("THEN link preview disappears") {
            userRobot.assertLinkPreviewInComposer(isDisplayed = false)
        }
        step("WHEN user taps on the send button") {
            userRobot.tapOnSendButton()
        }
        step("THEN user observes a message without link preview") {
            userRobot
                .assertMessage(youtubeVideoLink, isClickable = true)
                .assertLinkPreviewInMessageList(isDisplayed = false)
        }
    }

    @AllureId("6832")
    @Test
    fun test_giphyLinkPreview() {
        step("GIVEN user opens the channel") {
            userRobot.login().openChannel()
        }
        step("WHEN user types a giphy url") {
            userRobot.typeText(giphyGifLink)
        }
        step("THEN user observes a link preview") {
            userRobot.assertLinkPreviewInComposer(isDisplayed = true)
        }
        step("WHEN user taps on the send button") {
            userRobot.tapOnSendButton()
        }
        step("THEN user observes a message with link preview") {
            userRobot
                .assertMessage(giphyGifLink, isClickable = true)
                .assertLinkPreviewInMessageList(isDisplayed = true)
        }
    }

    @AllureId("6833")
    @Test
    fun test_giphyLinkWithoutPreview() {
        step("GIVEN user opens the channel") {
            userRobot.login().openChannel()
        }
        step("WHEN user types a giphy url") {
            userRobot.typeText(giphyGifLink)
        }
        step("AND user cancels the link preview") {
            userRobot.tapOnAttachmentCancelIcon()
        }
        step("THEN link preview disappears") {
            userRobot.assertLinkPreviewInComposer(isDisplayed = false)
        }
        step("WHEN user taps on the send button") {
            userRobot.tapOnSendButton()
        }
        step("THEN user observes a message without link preview") {
            userRobot
                .assertMessage(giphyGifLink, isClickable = true)
                .assertLinkPreviewInMessageList(isDisplayed = false)
        }
    }

    @AllureId("6834")
    @Test
    fun test_participantSendsLinkToUnsplash() {
        step("GIVEN user opens the channel") {
            userRobot.login().openChannel()
        }
        step("WHEN participant sends an unsplash url") {
            userRobot.sendMessage(unsplashImageLink)
        }
        step("THEN user observes a message with link preview") {
            userRobot
                .assertMessage(unsplashImageLink, isClickable = true)
                .assertLinkPreviewInMessageList(isDisplayed = true)
        }
    }

    @AllureId("6835")
    @Test
    fun test_participantSendsLinkToYoutube() {
        step("GIVEN user opens the channel") {
            userRobot.login().openChannel()
        }
        step("WHEN participant sends a youtube url") {
            userRobot.sendMessage(youtubeVideoLink)
        }
        step("THEN user observes a message with link preview") {
            userRobot
                .assertMessage(youtubeVideoLink, isClickable = true)
                .assertLinkPreviewInMessageList(isDisplayed = true)
        }
    }

    @AllureId("6836")
    @Test
    fun test_participantSendsLinkToGiphy() {
        step("GIVEN user opens the channel") {
            userRobot.login().openChannel()
        }
        step("WHEN participant sends a giphy url") {
            userRobot.sendMessage(giphyGifLink)
        }
        step("THEN user observes a message with link preview") {
            userRobot
                .assertMessage(giphyGifLink, isClickable = true)
                .assertLinkPreviewInMessageList(isDisplayed = true)
        }
    }
}
