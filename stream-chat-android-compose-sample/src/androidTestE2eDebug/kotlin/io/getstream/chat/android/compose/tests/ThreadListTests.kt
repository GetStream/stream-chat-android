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

import io.getstream.chat.android.compose.robots.assertThreadInThreadList
import io.getstream.chat.android.compose.robots.assertThreadListIsEmpty
import io.getstream.chat.android.compose.robots.assertThreadMessage
import io.getstream.chat.android.compose.sample.ui.InitTestActivity
import io.qameta.allure.kotlin.Allure.step
import io.qameta.allure.kotlin.AllureId
import org.junit.Test

/**
 * Covers the thread list screen, which the sample opens from the threads tab of the bottom bar.
 */
class ThreadListTests : StreamTestCase() {

    override fun initTestActivity() = InitTestActivity.UserLogin
    private val parentMessageText = "Test"
    private val replyText = "Reply"

    @AllureId("11564")
    @Test
    fun test_threadListIsEmpty_whenChannelHasNoThreads() {
        step("GIVEN user logs in") {
            userRobot.login().waitForChannelListToLoad()
        }
        step("WHEN user opens the thread list") {
            userRobot.openThreadList()
        }
        step("THEN the thread list is empty") {
            userRobot.assertThreadListIsEmpty()
        }
    }

    @AllureId("11570")
    @Test
    fun test_threadIsShownOnTheThreadList() {
        step("GIVEN user opens the channel") {
            userRobot.login().openChannel()
        }
        step("AND participant sends the message") {
            participantRobot.sendMessage(parentMessageText)
        }
        step("AND user replies to the message in the thread") {
            userRobot
                .openThread()
                .sendMessageInThread(replyText)
                .assertThreadMessage(replyText)
        }
        step("WHEN user opens the thread list") {
            userRobot
                .moveToChannelListFromThread()
                .openThreadList()
        }
        step("THEN the thread is shown with one reply") {
            userRobot.assertThreadInThreadList(parentMessageText, replies = 1)
        }
    }

    @AllureId("11576")
    @Test
    fun test_userOpensThreadFromTheThreadList() {
        step("GIVEN user opens the channel") {
            userRobot.login().openChannel()
        }
        step("AND participant sends the message") {
            participantRobot.sendMessage(parentMessageText)
        }
        step("AND user replies to the message in the thread") {
            userRobot
                .openThread()
                .sendMessageInThread(replyText)
                .assertThreadMessage(replyText)
        }
        step("AND user opens the thread list") {
            userRobot
                .moveToChannelListFromThread()
                .openThreadList()
                .assertThreadInThreadList(parentMessageText, replies = 1)
        }
        step("WHEN user taps on the thread") {
            userRobot.openThreadFromThreadList()
        }
        step("THEN the thread is opened on the reply") {
            userRobot.assertThreadMessage(replyText)
        }
    }
}
