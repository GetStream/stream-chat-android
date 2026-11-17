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
import io.getstream.chat.android.compose.pages.ScheduledMessagesPage.Screen
import io.getstream.chat.android.compose.robots.assertMessage
import io.getstream.chat.android.compose.robots.assertMessageCount
import io.getstream.chat.android.compose.robots.assertMessageWithImage
import io.getstream.chat.android.compose.robots.assertScheduleSheetIsNotShown
import io.getstream.chat.android.compose.robots.assertScheduleSheetLimitReached
import io.getstream.chat.android.compose.robots.assertScheduleTimeSheetError
import io.getstream.chat.android.compose.robots.assertScheduledMessage
import io.getstream.chat.android.compose.robots.assertScheduledMessageActions
import io.getstream.chat.android.compose.robots.assertScheduledMessageActionsAreClosed
import io.getstream.chat.android.compose.robots.assertScheduledMessageCanBeSaved
import io.getstream.chat.android.compose.robots.assertScheduledMessageCommandNotAllowed
import io.getstream.chat.android.compose.robots.assertScheduledMessageEditor
import io.getstream.chat.android.compose.robots.assertScheduledMessageFailed
import io.getstream.chat.android.compose.robots.assertScheduledMessageGoneNotice
import io.getstream.chat.android.compose.robots.assertScheduledMessageIsNotShown
import io.getstream.chat.android.compose.robots.assertScheduledMessageListedFromBanner
import io.getstream.chat.android.compose.robots.assertScheduledMessageQuoteDeletedBanner
import io.getstream.chat.android.compose.robots.assertScheduledMessageReply
import io.getstream.chat.android.compose.robots.assertScheduledMessageSendNowQueued
import io.getstream.chat.android.compose.robots.assertScheduledMessageSentSnackbar
import io.getstream.chat.android.compose.robots.assertScheduledMessagesAreEmpty
import io.getstream.chat.android.compose.robots.assertScheduledMessagesBanner
import io.getstream.chat.android.compose.robots.assertScheduledMessagesBannerIsHidden
import io.getstream.chat.android.compose.robots.assertScheduledMessagesLoadFailed
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
import io.qameta.allure.kotlin.AllureId
import org.junit.Test
import java.util.Calendar
import java.util.Date
import io.getstream.chat.android.compose.R as ComposeR

/**
 * Covers scheduled messages in the sample: a long press on send schedules the message for
 * tomorrow at 9:00, and the mock server sends it when a test asks for it, as when its time comes.
 */
class ScheduledMessagesTests : StreamTestCase() {

    override fun initTestActivity() = InitTestActivity.UserLogin
    override var useScheduledMessages = true
    private val sampleText = "Scheduled"

    @AllureId("12920")
    @Test
    fun test_scheduledMessageIsListedAndNotSent() {
        step("GIVEN user opens the channel") {
            userRobot.login().openChannel()
        }
        step("WHEN user schedules a message") {
            userRobot.scheduleMessage(sampleText)
        }
        step("THEN the banner shows when the message is sent") {
            userRobot.assertScheduledMessagesBanner(Banner.scheduledFor(tomorrowAt(9)))
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

    @AllureId("12921")
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

    @AllureId("12922")
    @Test
    fun test_scheduledMessageIsSentWhenItsTimeComes() {
        step("GIVEN user schedules a message") {
            userRobot.login().openChannel().scheduleMessage(sampleText)
        }
        step("AND the server has it") {
            backendRobot.waitForScheduledMessage(sampleText)
            userRobot.assertScheduledMessagesBanner(Banner.scheduledFor(tomorrowAt(9)))
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

    @AllureId("12923")
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
            backendRobot.waitForScheduledMessage(editedText)
        }
        step("AND the list shows the new text, at the same time") {
            userRobot
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
    fun test_editingAnotherScheduledMessageReplacesTheFirstInTheComposer() {
        val otherText = "Other"
        val editedText = "Edited"
        step("GIVEN user schedules two messages") {
            userRobot
                .login()
                .openChannel()
                .scheduleMessage(sampleText)
                .scheduleMessage(otherText)
            backendRobot.waitForScheduledMessage(otherText)
        }
        step("AND user starts editing the first one") {
            userRobot
                .openScheduledMessagesFromBanner()
                .startEditingScheduledMessage(sampleText)
        }
        step("WHEN user edits the second one instead, and saves it") {
            userRobot
                .startEditingScheduledMessage(otherText)
                .assertScheduledMessageEditor(otherText)
                .typeText(editedText)
                .saveScheduledMessage()
        }
        step("THEN the server has the edit of the second one only") {
            backendRobot.waitForScheduledMessage(editedText)
            backendRobot.waitForScheduledMessage(sampleText)
        }
        step("AND the list shows the first one unchanged, and the second one edited") {
            userRobot
                .assertScheduledMessage(sampleText)
                .assertScheduledMessage(editedText)
                .assertScheduledMessageIsNotShown(otherText)
        }
    }

    @Test
    fun test_scheduledMessageSentWhileItIsEditedCantBeSaved() {
        step("GIVEN user schedules a message") {
            userRobot.login().openChannel().scheduleMessage(sampleText)
            backendRobot.waitForScheduledMessage(sampleText)
        }
        step("AND user starts editing it") {
            userRobot
                .openScheduledMessagesFromBanner()
                .startEditingScheduledMessage(sampleText)
                .assertScheduledMessageCanBeSaved(true)
        }
        step("WHEN the server sends it") {
            backendRobot.sendScheduledMessages()
        }
        step("THEN the user is told it is gone, and can't save it") {
            userRobot
                .assertScheduledMessageGoneNotice()
                .assertScheduledMessageEditor(sampleText)
                .assertScheduledMessageCanBeSaved(false)
        }
    }

    @Test
    fun test_commandCantBeSavedInAScheduledMessage() {
        step("GIVEN user schedules a message") {
            userRobot.login().openChannel().scheduleMessage(sampleText)
            backendRobot.waitForScheduledMessage(sampleText)
        }
        step("WHEN user edits it to start with a command") {
            userRobot
                .openScheduledMessagesFromBanner()
                .startEditingScheduledMessage(sampleText)
                .typeText("/giphy")
        }
        step("THEN the user is told commands can't be scheduled, and can't save it") {
            userRobot
                .assertScheduledMessageCommandNotAllowed()
                .assertScheduledMessageCanBeSaved(false)
        }
    }

    @Test
    fun test_scheduledMessageFailedByTheServerIsEditedAndScheduledAgain() {
        val editedText = "Edited"
        step("GIVEN user schedules a message that the server fails to send") {
            userRobot.login().openChannel().scheduleMessage(sampleText)
            backendRobot.waitForScheduledMessage(sampleText)
            backendRobot.failScheduledMessage(failureReason = "internal_error")
        }
        step("WHEN user edits it and schedules it again tomorrow at 10:00") {
            userRobot
                .openScheduledMessagesFromBanner()
                .editAndRescheduleScheduledMessage(sampleText, editedText, hour = 10)
        }
        step("THEN the server has the new message") {
            backendRobot.waitForScheduledMessage(editedText)
        }
        step("AND the list shows the new message instead of the failed one") {
            userRobot
                .assertScheduledMessage(editedText, time = tomorrowAt(10))
                .assertScheduledMessageIsNotShown(sampleText)
        }
    }

    @AllureId("12924")
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

    @AllureId("12925")
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

    @AllureId("12926")
    @Test
    fun test_scheduledThreadReplyIsSentInTheThread() {
        val parentText = "Parent"
        val threadReplyPreview = "${Screen.threadReplyPrefix} $sampleText"
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
        step("THEN the thread banner shows when the reply is sent, and the reply is not in the thread") {
            backendRobot.waitForScheduledMessage(sampleText)
            userRobot
                .assertScheduledMessageListedFromBanner(threadReplyPreview)
                .assertScheduledMessagesBanner(Banner.scheduledFor(tomorrowAt(9)))
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

    @AllureId("12927")
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
            backendRobot.waitForScheduledMessage(sampleText)
            userRobot
                .assertScheduledMessagesBanner(Banner.scheduledFor(tomorrowAt(9)))
                .assertMessage(sampleText, isDisplayed = false)
        }
        step("WHEN the time of the message comes") {
            backendRobot.sendScheduledMessages()
        }
        step("THEN the message is in the channel with the image") {
            userRobot.assertMessageWithImage(sampleText)
        }
    }

    @AllureId("12928")
    @Test
    fun test_scheduledMessageWithFailedUploadIsRetried() {
        val uploadFailed = appContext.getString(ComposeR.string.stream_compose_scheduled_message_failure_upload)
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
                .assertScheduledMessageFailed(sampleText, uploadFailed)
        }
        step("WHEN uploads work again and user reschedules it at its date") {
            backendRobot.failUploads(enabled = false)
            userRobot.rescheduleScheduledMessageAtItsDate(sampleText)
        }
        step("THEN the message is scheduled") {
            backendRobot.waitForScheduledMessage(sampleText)
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

    @AllureId("12929")
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
        step("THEN the banner shows the message") {
            userRobot.assertScheduledMessagesBanner(Banner.scheduledFor(tomorrowAt(9)))
        }
        step("WHEN user comes back online") {
            device.enableInternetConnection()
        }
        step("THEN the message is scheduled once") {
            backendRobot.waitForScheduledMessage(sampleText)
            userRobot
                .assertScheduledMessagesBanner(Banner.scheduledFor(tomorrowAt(9)))
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

    @AllureId("12930")
    @Test
    fun test_scheduledMessageSentInTheBackgroundLeavesTheList() {
        step("GIVEN user schedules a message") {
            userRobot.login().openChannel().scheduleMessage(sampleText)
        }
        step("AND user goes to the background once the server has it") {
            backendRobot.waitForScheduledMessage(sampleText)
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

    @AllureId("12931")
    @Test
    fun test_scheduledMessageFailedByTheServerCanOnlyBeScheduledAgainOrDeleted() {
        val banned = appContext.getString(ComposeR.string.stream_compose_scheduled_message_failure_banned)
        step("GIVEN user schedules a message") {
            userRobot.login().openChannel().scheduleMessage(sampleText)
        }
        step("WHEN the server fails to send it, as the user is banned") {
            backendRobot.waitForScheduledMessage(sampleText)
            backendRobot.failScheduledMessage(failureReason = "banned")
        }
        step("THEN the list shows the reason") {
            userRobot
                .assertScheduledMessagesBanner(Banner.failed(1))
                .openScheduledMessagesFromBanner()
                .assertScheduledMessageFailed(sampleText, banned)
        }
        step("AND the message can only be edited and scheduled again, or deleted") {
            userRobot
                .openScheduledMessageActions(sampleText)
                .assertScheduledMessageActions(
                    shown = listOf(Actions.editAndReschedule, Actions.delete),
                    hidden = listOf(Actions.edit, Actions.reschedule, Actions.sendNow),
                )
        }
        step("WHEN user deletes it") {
            userRobot.deleteScheduledMessage(sampleText)
        }
        step("THEN the message leaves the list") {
            userRobot.assertScheduledMessagesAreEmpty()
        }
    }

    @AllureId("12932")
    @Test
    fun test_scheduledMessageOverTheChannelLimitFailsWithoutRetry() {
        val limitReached = appContext.getString(ComposeR.string.stream_compose_scheduled_message_failure_limit_channel)
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
                .assertScheduledMessageFailed(sampleText, limitReached)
        }
        step("AND the message can be edited, rescheduled or deleted, but not sent now") {
            userRobot
                .openScheduledMessageActions(sampleText)
                .assertScheduledMessageActions(
                    shown = listOf(Actions.edit, Actions.reschedule, Actions.delete),
                    hidden = listOf(Actions.sendNow),
                )
        }
    }

    @AllureId("12933")
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

    @AllureId("12934")
    @Test
    fun test_messageScheduledOnAnotherDeviceIsListed() {
        step("GIVEN user opens the channel") {
            userRobot.login().openChannel()
        }
        step("WHEN user schedules a message on another device") {
            backendRobot.createScheduledMessage(sampleText)
        }
        step("THEN the banner shows it") {
            userRobot.assertScheduledMessagesBanner(Banner.scheduledFor)
        }
        step("AND the list shows it") {
            userRobot
                .openScheduledMessagesFromBanner()
                .assertScheduledMessage(sampleText)
        }
    }

    @Test
    fun test_scheduledMessagesShowTheLoadErrorWhileOffline() {
        step("GIVEN user is logged in") {
            userRobot.login().waitForChannelListToLoad()
        }
        step("AND user goes offline") {
            device.disableInternetConnection()
        }
        step("WHEN user opens the scheduled messages") {
            userRobot.openScheduledMessages()
        }
        step("THEN the screen says they could not load, with Try again") {
            userRobot.assertScheduledMessagesLoadFailed()
        }
        step("WHEN user comes back online") {
            device.enableInternetConnection()
        }
        step("THEN the scheduled messages load") {
            userRobot.assertScheduledMessagesAreEmpty()
        }
    }

    @Test
    fun test_scheduledMessageOfADeletedChannelCanOnlyBeCopiedOrDeleted() {
        val channelDeleted =
            appContext.getString(ComposeR.string.stream_compose_scheduled_message_failure_channel_deleted)
        step("GIVEN user schedules a message") {
            userRobot.login().openChannel().scheduleMessage(sampleText)
            backendRobot.waitForScheduledMessage(sampleText)
        }
        step("WHEN the server fails to send it, as the channel was deleted") {
            backendRobot.failScheduledMessage(failureReason = "channel_deleted")
        }
        step("THEN the list shows the reason") {
            userRobot
                .assertScheduledMessagesBanner(Banner.failed(1))
                .openScheduledMessagesFromBanner()
                .assertScheduledMessageFailed(sampleText, channelDeleted)
        }
        step("AND its text can only be copied, or the message deleted") {
            userRobot
                .openScheduledMessageActions(sampleText)
                .assertScheduledMessageActions(
                    shown = listOf(Actions.copyText, Actions.delete),
                    hidden = listOf(Actions.edit, Actions.editAndReschedule, Actions.reschedule, Actions.sendNow),
                )
        }
    }

    @Test
    fun test_scheduledMessageSentWhileItsActionsAreOpenLeavesTheListWithANotice() {
        step("GIVEN user schedules a message") {
            userRobot.login().openChannel().scheduleMessage(sampleText)
            backendRobot.waitForScheduledMessage(sampleText)
        }
        step("AND user opens its actions in the scheduled messages") {
            userRobot
                .openScheduledMessagesFromBanner()
                .openScheduledMessageActions(sampleText)
        }
        step("WHEN the time of the message comes") {
            backendRobot.sendScheduledMessages()
        }
        step("THEN the actions close, and a notice says the message was already sent") {
            userRobot
                .assertScheduledMessageActionsAreClosed()
                .assertScheduledMessageGoneNotice()
        }
        step("AND the message left the list") {
            userRobot.assertScheduledMessagesAreEmpty()
        }
    }

    @Test
    fun test_scheduledMessageSentNowOfflineIsSentOnceOnline() {
        step("GIVEN user schedules a message") {
            userRobot.login().openChannel().scheduleMessage(sampleText)
            backendRobot.waitForScheduledMessage(sampleText)
        }
        step("AND user opens the scheduled messages and goes offline") {
            userRobot.openScheduledMessagesFromBanner()
            device.disableInternetConnection()
        }
        step("WHEN user sends it now") {
            userRobot.sendScheduledMessageNow(sampleText)
        }
        step("THEN a notice says it is sent once back online") {
            userRobot.assertScheduledMessageSendNowQueued()
        }
        step("WHEN user comes back online") {
            device.enableInternetConnection()
        }
        step("THEN the message leaves the list, and is in the channel") {
            userRobot
                .assertScheduledMessagesAreEmpty()
                .pressBack()
                .assertMessage(sampleText)
        }
    }

    @Test
    fun test_scheduledReplyToADeletedMessageCanOnlyBeEditedOrDeleted() {
        val parentText = "Parent"
        val quoteDeleted =
            appContext.getString(ComposeR.string.stream_compose_scheduled_message_failure_quote_deleted)
        step("GIVEN user schedules a reply to a message") {
            userRobot
                .login()
                .openChannel()
                .sendMessage(parentText)
                .selectReplyFromContextMenu()
                .scheduleMessage(sampleText)
            backendRobot.waitForScheduledMessage(sampleText)
        }
        step("THEN the list shows that it is a reply") {
            userRobot
                .openScheduledMessagesFromBanner()
                .assertScheduledMessageReply(sampleText)
                .pressBack()
        }
        step("WHEN user deletes the quoted message") {
            userRobot.deleteMessage(parentText)
        }
        step("THEN the list says the quote was deleted") {
            userRobot
                .openScheduledMessagesFromBanner()
                .assertScheduledMessageFailed(sampleText, quoteDeleted)
        }
        step("AND the reply can only be edited or deleted") {
            userRobot
                .openScheduledMessageActions(sampleText)
                .assertScheduledMessageActions(
                    shown = listOf(Actions.edit, Actions.delete),
                    hidden = listOf(Actions.reschedule, Actions.sendNow),
                )
        }
    }

    @Test
    fun test_scheduledReplyToADeletedMessageIsSavedOnceTheQuoteIsRemoved() {
        val parentText = "Parent"
        val editedText = "Edited"
        step("GIVEN user schedules a reply to a message that is deleted afterwards") {
            userRobot
                .login()
                .openChannel()
                .sendMessage(parentText)
                .selectReplyFromContextMenu()
                .scheduleMessage(sampleText)
            backendRobot.waitForScheduledMessage(sampleText)
            userRobot.deleteMessage(parentText)
        }
        step("WHEN user edits the reply") {
            userRobot
                .openScheduledMessagesFromBanner()
                .startEditingScheduledMessage(sampleText)
                .typeText(editedText)
        }
        step("THEN a banner says the quote was deleted, and the reply can't be saved") {
            userRobot
                .assertScheduledMessageQuoteDeletedBanner()
                .assertScheduledMessageCanBeSaved(false)
        }
        step("WHEN user removes the quote and saves the reply") {
            userRobot
                .removeScheduledMessageQuote()
                .assertScheduledMessageQuoteDeletedBanner(isDisplayed = false)
                .saveScheduledMessage()
        }
        step("THEN the server has the edit, without the quote") {
            backendRobot.waitForScheduledMessage(editedText)
            userRobot.assertScheduledMessage(editedText)
        }
    }

    @Test
    fun test_bannerCountsSeveralScheduledMessages() {
        step("GIVEN user opens the channel") {
            userRobot.login().openChannel()
        }
        step("WHEN user schedules two messages") {
            userRobot
                .scheduleMessage("First")
                .scheduleMessage("Second")
        }
        step("THEN the banner counts them, and its link opens all of them") {
            userRobot
                .assertScheduledMessagesBanner(Banner.messages(2).hasDescendant(Banner.viewAll))
                .openScheduledMessagesFromBanner()
                .assertScheduledMessage("First", time = tomorrowAt(9))
                .assertScheduledMessage("Second", time = tomorrowAt(9))
        }
    }

    @Test
    fun test_bannerShowsTheChannelLimitReached() {
        step("GIVEN user can schedule one message in the channel, as its config tells") {
            backendRobot.setScheduledMessagesLimit(1, inConfig = true)
        }
        step("WHEN user schedules a message") {
            userRobot.login().openChannel().scheduleMessage(sampleText)
        }
        step("THEN the banner shows the limit is reached, and its link opens all the messages") {
            userRobot.assertScheduledMessagesBanner(Banner.limitReached(count = 1, limit = 1).hasDescendant(Banner.viewAll))
        }
        step("AND the schedule options say the limit is reached") {
            userRobot
                .typeText("Another")
                .openScheduleSheet()
                .assertScheduleSheetLimitReached(limit = 1)
        }
    }

    @Test
    fun test_messageScheduledOfflineIsSentRightAwayWhenItsTimeHasPassed() {
        // At least the minimum lead time of one minute ahead once the sheet is confirmed.
        val sendAt = Calendar.getInstance().apply {
            add(Calendar.MINUTE, 3)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        step("GIVEN user opens the channel") {
            userRobot.login().openChannel()
        }
        step("AND user goes offline") {
            device.disableInternetConnection()
        }
        step("WHEN user schedules a message a few minutes ahead") {
            userRobot
                .typeText(sampleText)
                .scheduleComposerContentAt(sendAt.time)
                .assertScheduledMessagesBanner(Banner.scheduledFor(timeOf(sendAt.time)))
        }
        step("AND user comes back online once its time has passed") {
            userRobot.sleep(maxOf(0L, sendAt.timeInMillis - System.currentTimeMillis()) + 5.seconds)
            device.enableInternetConnection()
        }
        step("THEN a snackbar tells the scheduled message was sent, and the banner is gone") {
            userRobot
                .assertScheduledMessageSentSnackbar()
                .assertScheduledMessagesBannerIsHidden()
        }
        step("WHEN user taps View") {
            userRobot.openScheduledMessageSentFromSnackbar()
        }
        step("THEN the message is in the channel once") {
            userRobot
                .assertMessage(sampleText)
                .assertMessageCount(1)
        }
    }

    @Test
    fun test_messageIsScheduledAtACustomTime() {
        step("GIVEN user opens the channel") {
            userRobot.login().openChannel()
        }
        step("WHEN user schedules a message for tomorrow at 10:00 from the custom time") {
            userRobot
                .typeText(sampleText)
                .scheduleComposerContentAt(hour = 10)
        }
        step("THEN the list shows the message for tomorrow at 10:00") {
            userRobot
                .openScheduledMessagesFromBanner()
                .assertScheduledMessage(sampleText, time = tomorrowAt(10))
        }
    }

    @Test
    fun test_timeTooSoonCantBeScheduled() {
        val tooSoon = appContext.resources.getQuantityString(
            ComposeR.plurals.stream_compose_schedule_message_error_too_soon,
            1,
            1,
        )
        step("GIVEN user opens the custom time for a message") {
            userRobot
                .login()
                .openChannel()
                .typeText(sampleText)
                .openCustomTimeSheet()
        }
        step("WHEN user picks today at midnight, which has passed") {
            userRobot
                .typeScheduleDate(Date())
                .pickScheduleTime(hour = 0)
        }
        step("THEN the sheet tells the time is too soon, and can't schedule") {
            userRobot.assertScheduleTimeSheetError(tooSoon)
        }
    }

    @Test
    fun test_timeTooFarCantBeScheduled() {
        val tooFar = appContext.getString(ComposeR.string.stream_compose_schedule_message_error_too_far)
        val lastDay = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, MAX_LEAD_DAYS) }.time
        step("GIVEN user opens the custom time for a message") {
            userRobot
                .login()
                .openChannel()
                .typeText(sampleText)
                .openCustomTimeSheet()
        }
        step("WHEN user picks the last day the app allows, late at night") {
            userRobot
                .typeScheduleDate(lastDay)
                .pickScheduleTime(hour = 23, minute = 59)
        }
        step("THEN the sheet tells the time is too far ahead, and can't schedule") {
            userRobot.assertScheduleTimeSheetError(tooFar)
        }
    }

    @Test
    fun test_commandCantBeScheduled() {
        val commandError = appContext.getString(ComposeR.string.stream_compose_schedule_message_error_command)
        step("GIVEN user opens the channel") {
            userRobot.login().openChannel()
        }
        step("WHEN user opens the custom time for a message with a command") {
            userRobot
                .typeText("/giphy hi")
                .openCustomTimeSheet()
        }
        step("THEN the sheet tells commands can't be scheduled, and can't schedule") {
            userRobot.assertScheduleTimeSheetError(commandError)
        }
    }

    /** The time of tomorrow at [hour] o'clock, as the list shows it. */
    private fun tomorrowAt(hour: Int): String {
        val date = Calendar.getInstance().apply {
            add(Calendar.DAY_OF_YEAR, 1)
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, 0)
        }.time
        return timeOf(date)
    }

    private fun timeOf(date: Date): String = DateFormat.getTimeFormat(appContext).format(date)

    private companion object {
        /** How far ahead the server allows a message by default, as the mock channels set no limit. */
        const val MAX_LEAD_DAYS = 120
    }
}
