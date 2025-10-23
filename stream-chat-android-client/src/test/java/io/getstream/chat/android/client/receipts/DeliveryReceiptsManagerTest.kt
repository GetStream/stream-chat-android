/*
 * Copyright (c) 2014-2025 Stream.io Inc. All rights reserved.
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

package io.getstream.chat.android.client.receipts

import io.getstream.chat.android.DeliveryReceipts
import io.getstream.chat.android.PrivacySettings
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.models.Message
import io.getstream.chat.android.models.User
import io.getstream.chat.android.randomMessage
import io.getstream.chat.android.randomMessageList
import io.getstream.chat.android.randomUser
import io.getstream.chat.android.test.asCall
import org.junit.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoInteractions
import java.util.Date

internal class DeliveryReceiptsManagerTest {

    @Test
    fun `mark messages as delivered`() {
        val deliveredMessage = randomMessage()
        val messages = listOf(
            deliveredMessage,
            randomMessage(user = CurrentUser),
            randomMessage(type = "system"),
            randomMessage(deletedAt = Date()),
        )
        val fixture = Fixture()
        val sut = fixture.get()

        sut.markMessagesAsDelivered(messages)

        fixture.verifyMarkMessagesAsDelivered(listOf(deliveredMessage))
    }

    @Test
    fun `should not mark messages as delivered when current user is null`() {
        val messages = randomMessageList(10) { randomMessage() }
        val fixture = Fixture().givenCurrentUser(user = null)
        val sut = fixture.get()

        assertThrows<IllegalArgumentException>(message = "Cannot send delivery receipts: current user is null") {
            sut.markMessagesAsDelivered(messages)
        }
    }

    @Test
    fun `should skip mark messages as delivered when current user privacy settings are undefined`() {
        val currentUser = randomUser(privacySettings = null)
        val messages = randomMessageList(10) { randomMessage() }
        val fixture = Fixture().givenCurrentUser(currentUser)
        val sut = fixture.get()

        sut.markMessagesAsDelivered(messages)

        fixture.verifyNoInteractions()
    }

    @Test
    fun `should skip mark messages as delivered when delivery receipts are disabled`() {
        val currentUser = randomUser(
            privacySettings = PrivacySettings(
                deliveryReceipts = DeliveryReceipts(enabled = false),
            ),
        )
        val messages = randomMessageList(10) { randomMessage() }
        val fixture = Fixture().givenCurrentUser(currentUser)
        val sut = fixture.get()

        sut.markMessagesAsDelivered(messages)

        fixture.verifyNoInteractions()
    }

    @Test
    fun `should skip mark messages as delivered with empty list`() {
        val messages = emptyList<Message>()
        val fixture = Fixture()
        val sut = fixture.get()

        sut.markMessagesAsDelivered(messages)

        fixture.verifyNoInteractions()
    }

    @Test
    fun `should skip mark messages from the current user as delivered`() {
        val messages = randomMessageList(10) { randomMessage(user = CurrentUser) }
        val fixture = Fixture()
        val sut = fixture.get()

        sut.markMessagesAsDelivered(messages)

        fixture.verifyNoInteractions()
    }

    @Test
    fun `should skip mark system messages as delivered`() {
        val messages = randomMessageList(10) { randomMessage(type = "system") }
        val fixture = Fixture()
        val sut = fixture.get()

        sut.markMessagesAsDelivered(messages)

        fixture.verifyNoInteractions()
    }

    @Test
    fun `should skip mark deleted messages as delivered`() {
        val messages = randomMessageList(10) { randomMessage(deletedAt = Date()) }
        val fixture = Fixture()
        val sut = fixture.get()

        sut.markMessagesAsDelivered(messages)

        fixture.verifyNoInteractions()
    }

    private class Fixture {
        private val mockChatClient = mock<ChatClient> {
            on { markMessagesAsDelivered(any()) } doReturn Unit.asCall()
        }
        private var getCurrentUser: () -> User? = { CurrentUser }

        fun givenCurrentUser(user: User?) = apply {
            getCurrentUser = { user }
        }

        fun verifyNoInteractions() {
            verifyNoInteractions(mockChatClient)
        }

        fun verifyMarkMessagesAsDelivered(messages: List<Message>) {
            verify(mockChatClient).markMessagesAsDelivered(messages)
        }

        fun get() = DeliveryReceiptsManager(
            chatClient = mockChatClient,
            getCurrentUser = getCurrentUser,
        )
    }
}

private val CurrentUser = randomUser(
    privacySettings = PrivacySettings(
        deliveryReceipts = DeliveryReceipts(enabled = true),
    ),
)
