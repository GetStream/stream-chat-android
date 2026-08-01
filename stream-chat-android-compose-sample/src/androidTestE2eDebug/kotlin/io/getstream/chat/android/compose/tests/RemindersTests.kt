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

import io.getstream.chat.android.compose.robots.assertReminder
import io.getstream.chat.android.compose.robots.assertReminderSavedForLater
import io.getstream.chat.android.compose.robots.assertRemindersAreEmpty
import io.getstream.chat.android.compose.robots.assertRemindersScreen
import io.getstream.chat.android.compose.sample.ui.InitTestActivity
import io.qameta.allure.kotlin.Allure.step
import io.qameta.allure.kotlin.AllureId
import org.junit.Test

/**
 * Covers the message reminders screen, which the sample opens from the navigation drawer. The
 * sample cannot create a reminder, so the reminders are seeded on the server side.
 */
class RemindersTests : StreamTestCase() {

    override fun initTestActivity() = InitTestActivity.UserLogin
    private val sampleText = "Test"
    private val oneHourInSeconds = 3600

    @AllureId("11574")
    @Test
    fun test_remindersScreenIsEmpty_whenUserHasNoReminders() {
        step("GIVEN user logs in") {
            userRobot.login().waitForChannelListToLoad()
        }
        step("WHEN user opens the reminders screen") {
            userRobot.openReminders()
        }
        step("THEN no reminder is shown") {
            userRobot
                .assertRemindersScreen()
                .assertRemindersAreEmpty()
        }
    }

    @AllureId("11562")
    @Test
    fun test_reminderSavedForLaterIsShownOnTheRemindersScreen() {
        step("GIVEN user opens the channel") {
            userRobot.login().openChannel()
        }
        step("AND participant sends the message") {
            participantRobot.sendMessage(sampleText)
        }
        step("AND the message is saved for later") {
            backendRobot.createReminder()
        }
        step("WHEN user opens the reminders screen") {
            userRobot
                .moveToChannelListFromMessageList()
                .openReminders()
        }
        step("THEN the message is shown as saved for later") {
            userRobot
                .assertRemindersScreen()
                .assertReminderSavedForLater(sampleText)
        }
    }

    @AllureId("11571")
    @Test
    fun test_scheduledReminderIsShownOnTheRemindersScreen() {
        step("GIVEN user opens the channel") {
            userRobot.login().openChannel()
        }
        step("AND participant sends the message") {
            participantRobot.sendMessage(sampleText)
        }
        step("AND a reminder for the message is due in one hour") {
            backendRobot.createReminder(remindAtSeconds = oneHourInSeconds)
        }
        step("WHEN user opens the reminders screen") {
            userRobot
                .moveToChannelListFromMessageList()
                .openReminders()
        }
        step("THEN the message is shown on the reminders screen") {
            userRobot
                .assertRemindersScreen()
                .assertReminder(sampleText)
        }
    }
}
