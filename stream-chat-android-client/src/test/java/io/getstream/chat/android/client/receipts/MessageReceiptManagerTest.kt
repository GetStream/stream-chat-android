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
import io.getstream.chat.android.client.extensions.internal.NEVER
import io.getstream.chat.android.client.persistence.repository.MessageReceiptRepository
import io.getstream.chat.android.models.Message
import io.getstream.chat.android.models.User
import io.getstream.chat.android.randomChannel
import io.getstream.chat.android.randomChannelUserRead
import io.getstream.chat.android.randomConfig
import io.getstream.chat.android.randomDate
import io.getstream.chat.android.randomMessage
import io.getstream.chat.android.randomMessageList
import io.getstream.chat.android.randomUser
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.times
import org.mockito.kotlin.verifyBlocking
import org.mockito.verification.VerificationMode
import java.util.Date

internal class MessageReceiptManagerTest {

    @Test
    fun `store message delivery receipts success`() = runTest {
        val deliveredMessage = randomMessage(deletedAt = null, deletedForMe = false)
        val messages = listOf(
            deliveredMessage,
            randomMessage(user = CurrentUser),
            randomMessage(type = "system"),
            randomMessage(deletedAt = randomDate()),
        )
        val fixture = Fixture()
        val sut = fixture.get()

        sut.markMessagesAsDelivered(messages)

        val receipts = listOf(
            MessageReceipt(
                messageId = deliveredMessage.id,
                type = MessageReceipt.TYPE_DELIVERY,
                createdAt = Now,
                cid = deliveredMessage.cid,
            ),
        )
        fixture.verifyUpsertMessageReceiptsCalled(receipts = receipts)
    }

    @Test
    fun `should skip storing message delivery receipts when current user is null`() = runTest {
        val messages = randomMessageList(10) { randomMessage() }
        val fixture = Fixture().givenCurrentUser(user = null)
        val sut = fixture.get()

        sut.markMessagesAsDelivered(messages)

        fixture.verifyUpsertMessageReceiptsCalled(never())
    }

    @Test
    fun `should store message delivery receipts when current user privacy settings are undefined`() = runTest {
        val currentUser = randomUser(privacySettings = null)
        val messages = randomMessageList(10) { randomMessage(deletedAt = null, deletedForMe = false) }
        val fixture = Fixture().givenCurrentUser(currentUser)
        val sut = fixture.get()

        sut.markMessagesAsDelivered(messages)

        val receipts = messages.map { message ->
            MessageReceipt(
                messageId = message.id,
                type = MessageReceipt.TYPE_DELIVERY,
                createdAt = Now,
                cid = message.cid,
            )
        }
        fixture.verifyUpsertMessageReceiptsCalled(receipts = receipts)
    }

    @Test
    fun `should skip storing message delivery receipts when delivery receipts are disabled`() = runTest {
        val currentUser = randomUser(
            privacySettings = PrivacySettings(
                deliveryReceipts = DeliveryReceipts(enabled = false),
            ),
        )
        val messages = randomMessageList(10) { randomMessage() }
        val fixture = Fixture().givenCurrentUser(currentUser)
        val sut = fixture.get()

        sut.markMessagesAsDelivered(messages)

        fixture.verifyUpsertMessageReceiptsCalled(never())
    }

    @Test
    fun `should skip storing message delivery receipts with empty list`() = runTest {
        val messages = emptyList<Message>()
        val fixture = Fixture()
        val sut = fixture.get()

        sut.markMessagesAsDelivered(messages)

        fixture.verifyUpsertMessageReceiptsCalled(never())
    }

    @Test
    fun `should skip storing message delivery receipts from the current user`() = runTest {
        val messages = randomMessageList(10) { randomMessage(user = CurrentUser) }
        val fixture = Fixture()
        val sut = fixture.get()

        sut.markMessagesAsDelivered(messages)

        fixture.verifyUpsertMessageReceiptsCalled(never())
    }

    @Test
    fun `should skip storing message delivery receipts from system messages`() = runTest {
        val messages = randomMessageList(10) { randomMessage(type = "system") }
        val fixture = Fixture()
        val sut = fixture.get()

        sut.markMessagesAsDelivered(messages)

        fixture.verifyUpsertMessageReceiptsCalled(never())
    }

    @Test
    fun `should skip storing message delivery receipts from deleted messages`() = runTest {
        val messages = randomMessageList(10) { randomMessage(deletedAt = Date()) }
        val fixture = Fixture()
        val sut = fixture.get()

        sut.markMessagesAsDelivered(messages)

        fixture.verifyUpsertMessageReceiptsCalled(never())
    }

    @Test
    fun `store channel delivery receipts success`() = runTest {
        val deliveredMessage = randomMessage(
            createdAt = Now,
            deletedAt = null,
            deletedForMe = false,
        )
        val channel = randomChannel(
            messages = listOf(
                deliveredMessage,
                randomMessage(user = CurrentUser, createdAt = NEVER),
                randomMessage(type = "system", createdAt = NEVER),
                randomMessage(deletedAt = randomDate(), createdAt = NEVER),
            ),
            read = listOf(
                randomChannelUserRead(
                    user = CurrentUser,
                    lastRead = NEVER,
                    lastDeliveredAt = null,
                ),
            ),
        )
        val channels = listOf(channel)
        val fixture = Fixture()
        val sut = fixture.get()

        sut.markChannelsAsDelivered(channels)

        val receipts = listOf(
            MessageReceipt(
                messageId = deliveredMessage.id,
                type = MessageReceipt.TYPE_DELIVERY,
                createdAt = Now,
                cid = deliveredMessage.cid,
            ),
        )
        fixture.verifyUpsertMessageReceiptsCalled(receipts = receipts)
    }

    @Test
    fun `should skip storing channel delivery receipts when delivery events are disabled`() = runTest {
        val channel = randomChannel(config = randomConfig(deliveryEventsEnabled = false))
        val fixture = Fixture()
        val sut = fixture.get()

        sut.markChannelsAsDelivered(channels = listOf(channel))

        fixture.verifyUpsertMessageReceiptsCalled(never())
    }

    @Test
    fun `should skip storing channel delivery receipts when current user is null`() = runTest {
        val channel = randomChannel()
        val fixture = Fixture().givenCurrentUser(user = null)
        val sut = fixture.get()

        sut.markChannelsAsDelivered(channels = listOf(channel))

        fixture.verifyUpsertMessageReceiptsCalled(never())
    }

    @Test
    fun `should skip storing channel delivery receipts when user read is not found`() = runTest {
        val channel = randomChannel(
            messages = randomMessageList(10),
            read = listOf(randomChannelUserRead()),
        )
        val channels = listOf(channel)
        val fixture = Fixture()
        val sut = fixture.get()

        sut.markChannelsAsDelivered(channels)

        fixture.verifyUpsertMessageReceiptsCalled(never())
    }

    @Test
    fun `should skip storing channel delivery receipts when last message is not found`() = runTest {
        val channel = randomChannel(
            messages = emptyList(),
            read = listOf(randomChannelUserRead(user = CurrentUser)),
        )
        val channels = listOf(channel)
        val fixture = Fixture()
        val sut = fixture.get()

        sut.markChannelsAsDelivered(channels)

        fixture.verifyUpsertMessageReceiptsCalled(never())
    }

    @Test
    fun `should skip storing channel delivery receipts when last non-deleted message is not found`() = runTest {
        val channel = randomChannel(
            messages = randomMessageList(10) { randomMessage(deletedAt = Now) },
            read = listOf(randomChannelUserRead(user = CurrentUser)),
        )
        val channels = listOf(channel)
        val fixture = Fixture()
        val sut = fixture.get()

        sut.markChannelsAsDelivered(channels)

        fixture.verifyUpsertMessageReceiptsCalled(never())
    }

    @Test
    fun `should skip storing channel delivery receipts when last message is already read`() = runTest {
        val channel = randomChannel(
            messages = listOf(
                randomMessage(
                    createdAt = Now,
                    deletedAt = null,
                    deletedForMe = false,
                ),
            ),
            read = listOf(
                randomChannelUserRead(
                    user = CurrentUser,
                    lastRead = Now,
                    lastDeliveredAt = null,
                ),
            ),
        )
        val channels = listOf(channel)
        val fixture = Fixture()
        val sut = fixture.get()

        sut.markChannelsAsDelivered(channels)

        fixture.verifyUpsertMessageReceiptsCalled(never())
    }

    @Test
    fun `should skip storing channel delivery receipts when last message is already delivered`() = runTest {
        val channel = randomChannel(
            messages = listOf(
                randomMessage(
                    createdAt = Now,
                    deletedAt = null,
                    deletedForMe = false,
                ),
            ),
            read = listOf(
                randomChannelUserRead(
                    user = CurrentUser,
                    lastRead = NEVER,
                    lastDeliveredAt = Now,
                ),
            ),
        )
        val channels = listOf(channel)
        val fixture = Fixture()
        val sut = fixture.get()

        sut.markChannelsAsDelivered(channels)

        fixture.verifyUpsertMessageReceiptsCalled(never())
    }

    private class Fixture {
        private val mockMessageReceiptRepository = mock<MessageReceiptRepository>()
        private var getCurrentUser: () -> User? = { CurrentUser }

        fun givenCurrentUser(user: User?) = apply {
            getCurrentUser = { user }
        }

        fun verifyUpsertMessageReceiptsCalled(
            mode: VerificationMode = times(1),
            receipts: List<MessageReceipt>? = null,
        ) {
            verifyBlocking(mockMessageReceiptRepository, mode) {
                upsertMessageReceipts(receipts ?: any())
            }
        }

        fun get() = MessageReceiptManager(
            scope = CoroutineScope(UnconfinedTestDispatcher()),
            now = { Now },
            getCurrentUser = getCurrentUser,
            messageReceiptRepository = mockMessageReceiptRepository,
        )
    }
}

private val Now = Date()

private val CurrentUser = randomUser(
    privacySettings = PrivacySettings(
        deliveryReceipts = DeliveryReceipts(enabled = true),
    ),
)
