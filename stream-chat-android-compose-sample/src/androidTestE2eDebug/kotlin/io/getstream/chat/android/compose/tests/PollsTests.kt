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

import io.getstream.chat.android.compose.robots.assertMessageDeliveryStatus
import io.getstream.chat.android.compose.robots.assertPollClosed
import io.getstream.chat.android.compose.robots.assertPollMessage
import io.getstream.chat.android.compose.robots.assertPollOption
import io.getstream.chat.android.compose.robots.assertPollOptionVoteCount
import io.getstream.chat.android.compose.robots.assertPollResults
import io.getstream.chat.android.compose.sample.ui.InitTestActivity
import io.getstream.chat.android.e2e.test.mockserver.MessageDeliveryStatus
import io.getstream.chat.android.e2e.test.robots.ParticipantRobot
import io.qameta.allure.kotlin.Allure.step
import io.qameta.allure.kotlin.AllureId
import org.junit.Test

class PollsTests : StreamTestCase() {

    override fun initTestActivity() = InitTestActivity.UserLogin

    private val question = "Best color?"
    private val firstOption = "Red"
    private val secondOption = "Blue"
    private val options = listOf(firstOption, secondOption)

    @AllureId("11787")
    @Test
    fun test_pollMessageIsShown_whenUserCreatesPoll() {
        step("GIVEN user opens the channel") {
            userRobot.login().openChannel()
        }
        step("WHEN user creates a poll from the attachment picker") {
            userRobot.createPoll(question, options)
        }
        step("THEN the poll message shows the question and the options") {
            userRobot
                .assertPollMessage(question)
                .assertPollOption(firstOption)
                .assertPollOption(secondOption)
        }
    }

    @AllureId("11788")
    @Test
    fun test_optionIsChecked_whenUserVotesInPoll() {
        step("GIVEN user creates a poll") {
            userRobot.login().openChannel().createPoll(question, options)
        }
        step("WHEN user votes for an option") {
            userRobot.castPollVote(firstOption)
        }
        step("THEN the option is checked") {
            userRobot.assertPollOption(firstOption, isChecked = true)
        }
    }

    @AllureId("11789")
    @Test
    fun test_optionIsUnchecked_whenUserRemovesPollVote() {
        step("GIVEN user creates a poll and votes") {
            userRobot
                .login()
                .openChannel()
                .createPoll(question, options)
                .castPollVote(firstOption)
                .assertPollOption(firstOption, isChecked = true)
        }
        step("WHEN user taps on the voted option again") {
            userRobot.removePollVote(firstOption)
        }
        step("THEN the option is unchecked") {
            userRobot.assertPollOption(firstOption, isChecked = false)
        }
    }

    @AllureId("11790")
    @Test
    fun test_participantVoteIsShownInPollResults_whenParticipantVotesInPoll() {
        step("GIVEN user creates a poll") {
            userRobot.login().openChannel().createPoll(question, options)
        }
        step("AND participant votes for an option") {
            // The participant vote needs the poll message to be delivered first; a vote
            // arriving before the message is rejected by the server.
            userRobot.assertMessageDeliveryStatus(MessageDeliveryStatus.SENT)
            participantRobot.castPollVote(firstOption)
        }
        step("WHEN user opens the poll results") {
            // The results dialog renders the poll as selected at tap time, so the vote has
            // to be reflected in the message list before the dialog is opened.
            userRobot
                .assertPollOptionVoteCount(firstOption, count = 1)
                .openPollResults()
        }
        step("THEN the participant vote is shown") {
            userRobot.assertPollResults(voterName = ParticipantRobot.name)
        }
    }

    @AllureId("11791")
    @Test
    fun test_pollIsClosed_whenUserEndsPoll() {
        step("GIVEN user creates a poll") {
            userRobot.login().openChannel().createPoll(question, options)
        }
        step("WHEN user ends the poll") {
            userRobot.endPoll()
        }
        step("THEN the poll is shown as ended") {
            userRobot.assertPollClosed()
        }
    }
}
