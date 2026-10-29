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

import android.text.format.DateFormat
import io.getstream.chat.android.compose.pages.ScheduledMessagesPage.Actions
import io.getstream.chat.android.compose.pages.ScheduledMessagesPage.Banner
import io.getstream.chat.android.compose.robots.assertMessage
import io.getstream.chat.android.compose.robots.assertMessageCount
import io.getstream.chat.android.compose.robots.assertMessageWithImage
import io.getstream.chat.android.compose.robots.assertScheduleSheetIsNotShown
import io.getstream.chat.android.compose.robots.assertScheduledMessage
import io.getstream.chat.android.compose.robots.assertScheduledMessageActions
import io.getstream.chat.android.compose.robots.assertScheduledMessageFailed
import io.getstream.chat.android.compose.robots.assertScheduledMessageIsNotShown
import io.getstream.chat.android.compose.robots.assertScheduledMessageSaved
import io.getstream.chat.android.compose.robots.assertScheduledMessagesAreEmpty
import io.getstream.chat.android.compose.robots.assertScheduledMessagesBanner
import io.getstream.chat.android.compose.robots.assertScheduledMessagesBannerIsHidden
import io.getstream.chat.android.compose.robots.assertScheduledMessagesScreen
import io.getstream.chat.android.compose.robots.assertThreadMessage
import io.getstream.chat.android.compose.sample.ui.InitTestActivity
import io.getstream.chat.android.e2e.test.mockserver.AttachmentType
import io.getstream.chat.android.e2e.test.uiautomator.appContext
import io.getstream.chat.android.e2e.test.uiautomator.device
import io.getstream.chat.android.e2e.test.uiautomator.disableInternetConnection
import io.getstream.chat.android.e2e.test.uiautomator.enableInternetConnection
import io.getstream.chat.android.e2e.test.uiautomator.goToBackground
import io.getstream.chat.android.e2e.test.uiautomator.goToForeground
import io.getstream.chat.android.e2e.test.uiautomator.seconds
import io.qameta.allure.kotlin.Allure.step
import org.junit.Test
import java.util.Calendar
import io.getstream.chat.android.compose.sample.R as SampleR

/**
 * Covers scheduled messages in the sample: a long press on send schedules the message for
 * tomorrow at 9:00, and the mock server sends it when a test asks for it, as when its time comes.
 */
class ScheduledMessagesTests : StreamTestCase() {

    override fun initTestActivity() = InitTestActivity.UserLogin
    override var useScheduledMessages = true
    private val sampleText = "Scheduled"

    @Test
    fun test_scheduledMessageIsListedAndNotSent() {
        step("GIVEN user opens the channel") {
            userRobot.login().openChannel()
        }
        step("WHEN user schedules a message") {
            userRobot.scheduleMessage(sampleText)
        }
        step("THEN the banner counts the scheduled message") {
            userRobot.assertScheduledMessagesBanner(Banner.messages(1))
        }
        step("AND the message is not in the channel") {
            userRobot.assertMessage(sampleText, isDisplayed = false)
        }
        step("WHEN user opens the scheduled messages from the banner") {
            userRobot.openScheduledMessagesFromBanner()
        }
        step("THEN the message is listed for tomorrow at 9:00") {
            userRobot
                .assertScheduledMessagesScreen()
                .assertScheduledMessage(sampleText, time = tomorrowAt(9))
        }
    }

    @Test
    fun test_scheduledMessageIsSentNow() {
        step("GIVEN user schedules a message") {
            userRobot.login().openChannel().scheduleMessage(sampleText)
        }
        step("WHEN user sends it now from the scheduled messages") {
            userRobot
                .openScheduledMessagesFromBanner()
                .sendScheduledMessageNow(sampleText)
        }
        step("THEN the message leaves the list") {
            userRobot.assertScheduledMessagesAreEmpty()
        }
        step("AND the message is in the channel, and the banner is gone") {
            userRobot
                .pressBack()
                .assertMessage(sampleText)
                .assertScheduledMessagesBannerIsHidden()
        }
    }

    @Test
    fun test_scheduledMessageIsSentWhenItsTimeComes() {
        step("GIVEN user schedules a message") {
            userRobot.login().openChannel().scheduleMessage(sampleText)
        }
        step("AND the server has it") {
            userRobot
                .assertScheduledMessageSaved()
                .assertScheduledMessagesBanner(Banner.messages(1))
        }
        step("WHEN the time of the message comes") {
            backendRobot.sendScheduledMessages()
        }
        step("THEN the message is in the channel, and the banner is gone") {
            userRobot
                .assertMessage(sampleText)
                .assertScheduledMessagesBannerIsHidden()
        }
        step("AND the scheduled messages are empty") {
            userRobot
                .moveToChannelListFromMessageList()
                .openScheduledMessages()
                .assertScheduledMessagesAreEmpty()
        }
    }

    @Test
    fun test_scheduledMessageIsEdited() {
        val editedText = "Edited"
        step("GIVEN user schedules a message") {
            userRobot.login().openChannel().scheduleMessage(sampleText)
        }
        step("WHEN user edits its text") {
            userRobot
                .openScheduledMessagesFromBanner()
                .editScheduledMessage(sampleText, editedText)
        }
        step("THEN the server has the edit") {
            userRobot.assertScheduledMessageSaved()
        }
        step("AND the list shows the new text") {
            userRobot
                .pressBack()
                .assertScheduledMessage(editedText, time = tomorrowAt(9))
                .assertScheduledMessageIsNotShown(sampleText)
        }
        step("WHEN the time of the message comes") {
            backendRobot.sendScheduledMessages()
        }
        step("THEN the channel shows the new text") {
            userRobot
                .pressBack()
                .assertMessage(editedText)
                .assertMessage(sampleText, isDisplayed = false)
        }
    }

    @Test
    fun test_scheduledMessageIsRescheduled() {
        step("GIVEN user schedules a message for tomorrow at 9:00") {
            userRobot.login().openChannel().scheduleMessage(sampleText)
        }
        step("WHEN user reschedules it to 10:00") {
            userRobot
                .openScheduledMessagesFromBanner()
                .rescheduleScheduledMessage(sampleText, hour = 10)
        }
        step("THEN the list shows the new time") {
            userRobot.assertScheduledMessage(sampleText, time = tomorrowAt(10))
        }
    }

    @Test
    fun test_scheduledMessageIsDeleted() {
        step("GIVEN user schedules a message") {
            userRobot.login().openChannel().scheduleMessage(sampleText)
        }
        step("WHEN user deletes it") {
            userRobot
                .openScheduledMessagesFromBanner()
                .deleteScheduledMessage(sampleText)
        }
        step("THEN the message leaves the list") {
            userRobot.assertScheduledMessagesAreEmpty()
        }
        step("WHEN the server sends the pending scheduled messages") {
            backendRobot.sendScheduledMessages()
        }
        step("THEN the message is never sent, and the banner is gone") {
            userRobot
                .pressBack()
                .sleep(2.seconds)
                .assertMessage(sampleText, isDisplayed = false)
                .assertScheduledMessagesBannerIsHidden()
        }
    }

    @Test
    fun test_scheduledThreadReplyIsSentInTheThread() {
        val parentText = "Parent"
        step("GIVEN user opens a thread") {
            userRobot
                .login()
                .openChannel()
                .sendMessage(parentText)
                .openThread()
        }
        step("WHEN user schedules a reply") {
            userRobot.scheduleMessage(sampleText)
        }
        step("THEN the thread banner counts the reply, and the reply is not in the thread") {
            userRobot
                .assertScheduledMessageSaved()
                .assertScheduledMessagesBanner(Banner.replies(1))
                .assertMessage(sampleText, isDisplayed = false)
        }
        step("WHEN the time of the reply comes") {
            backendRobot.sendScheduledMessages()
        }
        step("THEN the reply is in the thread") {
            userRobot
                .assertThreadMessage(sampleText)
                .assertScheduledMessagesBannerIsHidden()
        }
    }

    @Test
    fun test_scheduledMessageWithImageIsSentWithTheImage() {
        step("GIVEN user opens the channel") {
            userRobot.login().openChannel()
        }
        step("WHEN user schedules a message with an image") {
            userRobot
                .attachFile(AttachmentType.IMAGE)
                .typeText(sampleText)
                .scheduleComposerContent()
        }
        step("THEN the message is scheduled, and not in the channel") {
            userRobot
                .assertScheduledMessageSaved()
                .assertScheduledMessagesBanner(Banner.messages(1))
                .assertMessage(sampleText, isDisplayed = false)
        }
        step("WHEN the time of the message comes") {
            backendRobot.sendScheduledMessages()
        }
        step("THEN the message is in the channel with the image") {
            userRobot.assertMessageWithImage(sampleText)
        }
    }

    @Test
    fun test_scheduledMessageWithFailedUploadIsRetried() {
        val uploadFailed = appContext.getString(SampleR.string.scheduled_messages_failure_attachment_upload)
        step("GIVEN uploads fail") {
            backendRobot.failUploads()
        }
        step("AND user schedules a message with an image") {
            userRobot
                .login()
                .openChannel()
                .attachFile(AttachmentType.IMAGE)
                .typeText(sampleText)
                .scheduleComposerContent()
        }
        step("THEN the banner shows the failure, and the message is not in the channel") {
            userRobot
                .assertScheduledMessagesBanner(Banner.failed(1))
                .assertMessage(sampleText, isDisplayed = false)
        }
        step("AND the list shows the upload failure") {
            userRobot
                .openScheduledMessagesFromBanner()
                .assertScheduledMessageFailed(uploadFailed)
        }
        step("WHEN uploads work again and user retries") {
            backendRobot.failUploads(enabled = false)
            userRobot.retryScheduledMessage(uploadFailed)
        }
        step("THEN the message is scheduled") {
            userRobot.assertScheduledMessage(sampleText, time = tomorrowAt(9))
        }
        step("WHEN the time of the message comes") {
            backendRobot.sendScheduledMessages()
        }
        step("THEN the message is in the channel with the image") {
            userRobot
                .pressBack()
                .assertMessageWithImage(sampleText)
        }
    }

    @Test
    fun test_messageScheduledOfflineIsScheduledOnceOnReconnect() {
        step("GIVEN user opens the channel") {
            userRobot.login().openChannel()
        }
        step("AND user goes offline") {
            device.disableInternetConnection()
        }
        step("WHEN user schedules a message") {
            userRobot.scheduleMessage(sampleText)
        }
        step("THEN the banner shows it waits for the connection") {
            userRobot.assertScheduledMessagesBanner(Banner.waiting(1))
        }
        step("WHEN user comes back online") {
            device.enableInternetConnection()
        }
        step("THEN the message is scheduled once") {
            userRobot
                .assertScheduledMessagesBanner(Banner.messages(1))
                .openScheduledMessagesFromBanner()
                .assertScheduledMessage(sampleText, time = tomorrowAt(9))
        }
        step("WHEN the time of the message comes") {
            backendRobot.sendScheduledMessages()
        }
        step("THEN the message is sent once") {
            userRobot
                .pressBack()
                .assertMessage(sampleText)
                .sleep(2.seconds)
                .assertMessageCount(1)
        }
    }

    @Test
    fun test_scheduledMessageSentInTheBackgroundLeavesTheList() {
        step("GIVEN user schedules a message") {
            userRobot.login().openChannel().scheduleMessage(sampleText)
        }
        step("AND user goes to the background once the server has it") {
            userRobot.assertScheduledMessageSaved()
            device.goToBackground()
            backendRobot.waitForWebSocketDisconnection()
        }
        step("WHEN the time of the message comes") {
            backendRobot.sendScheduledMessages()
        }
        step("AND user comes back to the app") {
            device.goToForeground()
        }
        step("THEN the message is in the channel") {
            userRobot.assertMessage(sampleText)
        }
        step("AND the message left the scheduled messages") {
            userRobot
                .moveToChannelListFromMessageList()
                .openScheduledMessages()
                .assertScheduledMessagesAreEmpty()
        }
    }

    @Test
    fun test_scheduledMessageFailedByTheServerCanOnlyBeScheduledAgainOrDeleted() {
        val banned = appContext.getString(SampleR.string.scheduled_messages_failure_banned)
        step("GIVEN user schedules a message") {
            userRobot.login().openChannel().scheduleMessage(sampleText)
        }
        step("WHEN the server fails to send it, as the user is banned") {
            userRobot.assertScheduledMessageSaved()
            backendRobot.failScheduledMessage(failureReason = "banned")
        }
        step("THEN the list shows the reason") {
            userRobot
                .assertScheduledMessagesBanner(Banner.failed(1))
                .openScheduledMessagesFromBanner()
                .assertScheduledMessageFailed(banned)
        }
        step("AND the message can't be edited, sent now or retried") {
            userRobot
                .openScheduledMessageActions(banned)
                .assertScheduledMessageActions(
                    shown = listOf(Actions.scheduleAgain, Actions.delete),
                    hidden = listOf(Actions.edit, Actions.reschedule, Actions.sendNow, Actions.retry),
                )
        }
        step("WHEN user deletes it") {
            userRobot.deleteScheduledMessage(banned)
        }
        step("THEN the message leaves the list") {
            userRobot.assertScheduledMessagesAreEmpty()
        }
    }

    @Test
    fun test_scheduledMessageOverTheChannelLimitFailsWithoutRetry() {
        val limitReached = appContext.getString(SampleR.string.scheduled_messages_failure_limit_channel)
        step("GIVEN user can't schedule more messages in the channel") {
            backendRobot.setScheduledMessagesLimit(0)
        }
        step("WHEN user schedules a message") {
            userRobot.login().openChannel().scheduleMessage(sampleText)
        }
        step("THEN the list shows the limit") {
            userRobot
                .assertScheduledMessagesBanner(Banner.failed(1))
                .openScheduledMessagesFromBanner()
                .assertScheduledMessageFailed(limitReached)
        }
        step("AND the message can't be retried") {
            userRobot
                .openScheduledMessageActions(limitReached)
                .assertScheduledMessageActions(
                    shown = listOf(Actions.edit, Actions.delete),
                    hidden = listOf(Actions.retry),
                )
        }
    }

    @Test
    fun test_scheduleOptionIsHiddenWhenTheFeatureIsOff() {
        step("GIVEN scheduled messages are off for the app") {
            backendRobot.disableScheduledMessages()
        }
        step("AND user opens the channel") {
            userRobot.login().openChannel()
        }
        step("WHEN user long presses send") {
            userRobot
                .typeText(sampleText)
                .longPressSendButton()
        }
        step("THEN the schedule options are not shown") {
            userRobot.assertScheduleSheetIsNotShown()
        }
    }

    @Test
    fun test_messageScheduledOnAnotherDeviceIsListed() {
        step("GIVEN user opens the channel") {
            userRobot.login().openChannel()
        }
        step("WHEN user schedules a message on another device") {
            backendRobot.createScheduledMessage(sampleText)
        }
        step("THEN the banner counts it") {
            userRobot.assertScheduledMessagesBanner(Banner.messages(1))
        }
        step("AND the list shows it") {
            userRobot
                .openScheduledMessagesFromBanner()
                .assertScheduledMessage(sampleText)
        }
    }

    /** The time of tomorrow at [hour] o'clock, as the list shows it. */
    private fun tomorrowAt(hour: Int): String {
        val date = Calendar.getInstance().apply {
            add(Calendar.DAY_OF_YEAR, 1)
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, 0)
        }.time
        return DateFormat.getTimeFormat(appContext).format(date)
    }
}
