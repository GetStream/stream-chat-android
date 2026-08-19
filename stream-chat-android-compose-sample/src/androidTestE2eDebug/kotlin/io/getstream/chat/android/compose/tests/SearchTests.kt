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

import io.getstream.chat.android.compose.robots.assertMessage
import io.getstream.chat.android.compose.robots.assertMessageInChannelPreview
import io.getstream.chat.android.compose.sample.ui.InitTestActivity
import io.qameta.allure.kotlin.Allure.step
import io.qameta.allure.kotlin.AllureId
import org.junit.Test

/**
 * Covers the message search wired into the channel list header, the sample's default search mode.
 * Channel search, behind the channel search flag, is covered by [ChannelSearchTests].
 */
class SearchTests : StreamTestCase() {

    override fun initTestActivity() = InitTestActivity.UserLogin
    private val sampleText = "Test"

    @AllureId("5935")
    @Test
    fun test_userSearchesForMessage() {
        step("GIVEN user opens the channel") {
            userRobot.login().openChannel()
        }
        step("AND participant sends the message") {
            participantRobot.sendMessage(sampleText)
        }
        step("WHEN user searches for the message on the channel list") {
            userRobot
                .moveToChannelListFromMessageList()
                .searchForMessage(sampleText)
        }
        step("THEN the message is shown in the search results") {
            userRobot.assertMessageInChannelPreview(sampleText, fromCurrentUser = false)
        }
    }

    @AllureId("11561")
    @Test
    fun test_userOpensMessageFromSearchResults() {
        step("GIVEN user opens the channel") {
            userRobot.login().openChannel()
        }
        step("AND participant sends the message") {
            participantRobot.sendMessage(sampleText)
        }
        step("AND user searches for the message on the channel list") {
            userRobot
                .moveToChannelListFromMessageList()
                .searchForMessage(sampleText)
                .assertMessageInChannelPreview(sampleText, fromCurrentUser = false)
        }
        step("WHEN user taps on the search result") {
            userRobot.tapOnSearchResult()
        }
        step("THEN the message list is opened on the message") {
            userRobot
                .waitForMessageListToLoad()
                .assertMessage(sampleText)
        }
    }
}
