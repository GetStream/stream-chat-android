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
import io.getstream.chat.android.compose.robots.assertPushNotification
import io.getstream.chat.android.compose.robots.assertPushNotificationDoesNotAppear
import io.getstream.chat.android.compose.sample.ui.InitTestActivity
import io.getstream.chat.android.e2e.test.uiautomator.device
import io.getstream.chat.android.e2e.test.uiautomator.goToBackground
import io.getstream.chat.android.e2e.test.uiautomator.goToForeground
import io.getstream.chat.android.e2e.test.uiautomator.packageName
import io.qameta.allure.kotlin.Allure.step
import io.qameta.allure.kotlin.AllureId
import org.junit.Test

class PushNotificationTests : StreamTestCase() {

    override fun initTestActivity() = InitTestActivity.UserLogin

    private val pushMessage = "Push test message"
    private val pushReceiverComponent get() = "$packageName/$PUSH_RECEIVER_CLASS"

    @AllureId("5715")
    @Test
    fun test_pushNotificationFromMessageList() {
        step("GIVEN user opens the channel") {
            userRobot.login().openChannel()
        }
        step("AND user goes to background") {
            device.goToBackground()
        }
        step("WHEN participant sends a message and its push notification") {
            participantRobot
                .sendMessage(pushMessage)
                .sendPushNotification(pushReceiverComponent)
        }
        step("THEN user receives the push notification") {
            userRobot.assertPushNotification(pushMessage)
        }
        step("WHEN user taps on the push notification") {
            userRobot.tapOnPushNotification(pushMessage)
        }
        step("THEN the message list shows the message") {
            userRobot.assertMessage(pushMessage)
        }
    }

    @AllureId("5832")
    @Test
    fun test_pushNotificationFromChannelList() {
        step("GIVEN user opens the channel and goes back to the channel list") {
            userRobot.login().openChannel().tapOnBackButton()
        }
        step("AND user goes to background") {
            device.goToBackground()
        }
        step("WHEN participant sends a message and its push notification") {
            participantRobot
                .sendMessage(pushMessage)
                .sendPushNotification(pushReceiverComponent)
        }
        step("THEN user receives the push notification") {
            userRobot.assertPushNotification(pushMessage)
        }
        step("WHEN user taps on the push notification") {
            userRobot.tapOnPushNotification(pushMessage)
        }
        step("THEN the message list shows the message") {
            userRobot.assertMessage(pushMessage)
        }
    }

    @AllureId("5662")
    @Test
    fun test_pushNotification_optionalValuesEqualToNil() {
        assertPushNotificationWithDegradedPayload(rest = "null")
    }

    @AllureId("5834")
    @Test
    fun test_pushNotification_optionalValuesAreEmpty() {
        assertPushNotificationWithDegradedPayload(rest = "empty")
    }

    @AllureId("5835")
    @Test
    fun test_pushNotification_optionalValuesContainIncorrectType() {
        assertPushNotificationWithDegradedPayload(rest = "incorrect_type")
    }

    @AllureId("5836")
    @Test
    fun test_pushNotification_optionalValuesContainIncorrectData() {
        assertPushNotificationWithDegradedPayload(rest = "incorrect_data")
    }

    @AllureId("5837")
    @Test
    fun test_pushNotification_requiredValuesAreInvalid() {
        step("GIVEN user opens the channel") {
            userRobot.login().openChannel()
        }
        step("AND user goes to background") {
            device.goToBackground()
        }
        listOf("invalid_version", "invalid_sender", "invalid_type").forEach { rest ->
            step("WHEN participant sends a push notification with $rest in the payload") {
                participantRobot
                    .sendMessage(rest)
                    .sendPushNotification(pushReceiverComponent, rest = rest)
            }
            step("THEN user does not receive a push notification") {
                userRobot.assertPushNotificationDoesNotAppear(rest)
            }
        }
        step("WHEN user comes back to foreground") {
            device.goToForeground()
        }
        step("THEN the message list shows all messages") {
            userRobot
                .assertMessage("invalid_version")
                .assertMessage("invalid_sender")
                .assertMessage("invalid_type")
        }
    }

    /**
     * Verifies a push notification is still delivered and opens the message when the payload's
     * optional values are degraded but its required keys stay valid.
     *
     * [rest] selects how the mock server degrades the payload:
     * - `null`: omits the optional title and body
     * - `empty`: sends an empty title and body
     * - `incorrect_type`: sends a wrong-type title and junk badge fields
     * - `incorrect_data`: sends out-of-range badge fields
     *
     * In all of these the required keys (`version`, `sender`, `type`, `message_id`, `cid`) stay
     * valid, so the client accepts the push, shows it, and opens the message on tap. Payloads that
     * break a required key are covered by [test_pushNotification_requiredValuesAreInvalid].
     *
     * @param rest The mock server's payload-degradation mode.
     */
    private fun assertPushNotificationWithDegradedPayload(rest: String) {
        step("GIVEN user opens the channel") {
            userRobot.login().openChannel()
        }
        step("AND user goes to background") {
            device.goToBackground()
        }
        step("WHEN participant sends a message and a push notification with $rest optional values") {
            participantRobot
                .sendMessage(pushMessage)
                .sendPushNotification(pushReceiverComponent, rest = rest)
        }
        step("THEN user receives the push notification") {
            userRobot.assertPushNotification(pushMessage)
        }
        step("WHEN user taps on the push notification") {
            userRobot.tapOnPushNotification(pushMessage)
        }
        step("THEN the message list shows the message") {
            userRobot.assertMessage(pushMessage)
        }
    }
}

private const val PUSH_RECEIVER_CLASS = "io.getstream.chat.android.compose.sample.push.PushTestReceiver"
