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
import io.getstream.chat.android.client.api.ChatApi
import io.getstream.chat.android.client.extensions.internal.NEVER
import io.getstream.chat.android.client.persistance.repository.RepositoryFacade
import io.getstream.chat.android.client.persistence.repository.MessageReceiptRepository
import io.getstream.chat.android.models.User
import io.getstream.chat.android.randomChannel
import io.getstream.chat.android.randomChannelUserRead
import io.getstream.chat.android.randomConfig
import io.getstream.chat.android.randomMessage
import io.getstream.chat.android.randomMute
import io.getstream.chat.android.randomUser
import io.getstream.chat.android.test.asCall
import io.getstream.result.Error
import kotlinx.coroutines.test.runTest
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.times
import org.mockito.kotlin.verifyBlocking
import org.mockito.kotlin.wheneverBlocking
import org.mockito.verification.VerificationMode
import java.util.Date

internal class MessageReceiptManagerTest {

    @Test
    fun `store message delivery receipt when channel is found from repository`() = runTest {
        val message = DeliverableMessage
        val fixture = Fixture()
        val sut = fixture.get()

        sut.markMessageAsDelivered(message)

        val receipts = listOf(
            MessageReceipt(
                messageId = message.id,
                cid = message.cid,
                createdAt = Now,
            ),
        )
        fixture.verifyUpsertMessageReceiptsCalled(receipts = receipts)
    }

    @Test
    fun `fetch channel from API when channel is not found from repository`() = runTest {
        val message = DeliverableMessage
        val fixture = Fixture()
            .givenChannelNotFoundFromRepository()
        val sut = fixture.get()

        sut.markMessageAsDelivered(message)

        val receipts = listOf(
            MessageReceipt(
                messageId = message.id,
                cid = message.cid,
                createdAt = Now,
            ),
        )
        fixture.verifyUpsertMessageReceiptsCalled(receipts = receipts)
    }

    @Test
    fun `fetch message from API when message is not found from repository`() = runTest {
        val message = DeliverableMessage
        val fixture = Fixture()
            .givenMessageNotFoundFromRepository()
        val sut = fixture.get()

        sut.markMessageAsDelivered(messageId = message.id)

        val receipts = listOf(
            MessageReceipt(
                messageId = message.id,
                cid = message.cid,
                createdAt = Now,
            ),
        )
        fixture.verifyUpsertMessageReceiptsCalled(receipts = receipts)
    }

    @Test
    fun `should skip storing message delivery receipt when message is not found`() = runTest {
        val message = DeliverableMessage
        val fixture = Fixture()
            .givenMessageNotFoundFromRepository()
            .givenMessageNotFoundFromApi()
        val sut = fixture.get()

        sut.markMessageAsDelivered(messageId = message.id)

        fixture.verifyUpsertMessageReceiptsCalled(never())
    }

    @Test
    fun `should skip storing message delivery receipt when channel is not found`() = runTest {
        val message = DeliverableMessage
        val fixture = Fixture()
            .givenChannelNotFoundFromRepository()
            .givenChannelNotFoundFromApi()
        val sut = fixture.get()

        sut.markMessageAsDelivered(message)

        fixture.verifyUpsertMessageReceiptsCalled(never())
    }

    @Test
    fun `should skip storing message delivery receipt when current user is null`() = runTest {
        val message = DeliverableMessage
        val fixture = Fixture().givenCurrentUser(user = null)
        val sut = fixture.get()

        sut.markMessageAsDelivered(message)

        fixture.verifyUpsertMessageReceiptsCalled(never())
    }

    @Test
    fun `should store message delivery receipt when current user privacy settings are undefined`() = runTest {
        val currentUser = CurrentUser.copy(privacySettings = null)
        val message = DeliverableMessage
        val fixture = Fixture().givenCurrentUser(currentUser)
        val sut = fixture.get()

        sut.markMessageAsDelivered(message)

        val receipts = listOf(
            MessageReceipt(
                messageId = message.id,
                cid = message.cid,
                createdAt = Now,
            ),
        )
        fixture.verifyUpsertMessageReceiptsCalled(receipts = receipts)
    }

    @Test
    fun `should skip storing message delivery receipt when delivery receipts are disabled`() = runTest {
        val currentUser = CurrentUser.copy(
            privacySettings = PrivacySettings(
                deliveryReceipts = DeliveryReceipts(enabled = false),
            ),
        )
        val message = DeliverableMessage
        val fixture = Fixture().givenCurrentUser(currentUser)
        val sut = fixture.get()

        sut.markMessageAsDelivered(message)

        fixture.verifyUpsertMessageReceiptsCalled(never())
    }

    @Test
    fun `should skip storing message delivery receipt from the current user`() = runTest {
        val message = DeliverableMessage.copy(user = CurrentUser)
        val fixture = Fixture()
        val sut = fixture.get()

        sut.markMessageAsDelivered(message)

        fixture.verifyUpsertMessageReceiptsCalled(never())
    }

    @Test
    fun `should skip storing message delivery receipt from shadow banned messages`() = runTest {
        val message = DeliverableMessage.copy(shadowed = true)
        val fixture = Fixture()
        val sut = fixture.get()

        sut.markMessageAsDelivered(message)

        fixture.verifyUpsertMessageReceiptsCalled(never())
    }

    @Test
    fun `should skip storing message delivery receipt from muted users`() = runTest {
        val message = DeliverableMessage
        val currentUser = CurrentUser.copy(
            mutes = listOf(randomMute(user = CurrentUser, target = message.user)),
        )
        val fixture = Fixture()
            .givenCurrentUser(currentUser)
        val sut = fixture.get()

        sut.markMessageAsDelivered(message)

        fixture.verifyUpsertMessageReceiptsCalled(never())
    }

    @Test
    fun `store channel delivery receipts success`() = runTest {
        val message = DeliverableMessage
        val channel = DeliverableChannel
        val fixture = Fixture()
        val sut = fixture.get()

        sut.markChannelsAsDelivered(channels = listOf(channel))

        val receipts = listOf(
            MessageReceipt(
                messageId = message.id,
                cid = message.cid,
                createdAt = Now,
            ),
        )
        fixture.verifyUpsertMessageReceiptsCalled(receipts = receipts)
    }

    @Test
    fun `should skip storing channel delivery receipts when delivery events are disabled`() = runTest {
        val channel = DeliverableChannel.copy(config = randomConfig(deliveryEventsEnabled = false))
        val fixture = Fixture()
        val sut = fixture.get()

        sut.markChannelsAsDelivered(channels = listOf(channel))

        fixture.verifyUpsertMessageReceiptsCalled(never())
    }

    @Test
    fun `should skip storing channel delivery receipts when current user is null`() = runTest {
        val channel = DeliverableChannel
        val fixture = Fixture().givenCurrentUser(user = null)
        val sut = fixture.get()

        sut.markChannelsAsDelivered(channels = listOf(channel))

        fixture.verifyUpsertMessageReceiptsCalled(never())
    }

    @Test
    fun `should skip storing channel delivery receipts when user read is not found`() = runTest {
        val channel = DeliverableChannel.copy(read = listOf(randomChannelUserRead()))
        val fixture = Fixture()
        val sut = fixture.get()

        sut.markChannelsAsDelivered(channels = listOf(channel))

        fixture.verifyUpsertMessageReceiptsCalled(never())
    }

    @Test
    fun `should skip storing channel delivery receipts when last message is not found`() = runTest {
        val channel = DeliverableChannel.copy(messages = emptyList())
        val fixture = Fixture()
        val sut = fixture.get()

        sut.markChannelsAsDelivered(channels = listOf(channel))

        fixture.verifyUpsertMessageReceiptsCalled(never())
    }

    @Test
    fun `should skip storing channel delivery receipts when last non-deleted message is not found`() = runTest {
        val channel = DeliverableChannel.copy(messages = listOf(randomMessage(deletedAt = Now)))
        val fixture = Fixture()
        val sut = fixture.get()

        sut.markChannelsAsDelivered(channels = listOf(channel))

        fixture.verifyUpsertMessageReceiptsCalled(never())
    }

    @Test
    fun `should skip storing channel delivery receipts when last message is already read`() = runTest {
        val channel = DeliverableChannel.copy(
            read = listOf(
                randomChannelUserRead(
                    user = CurrentUser,
                    lastRead = Now,
                    lastDeliveredAt = null,
                ),
            ),
        )
        val fixture = Fixture()
        val sut = fixture.get()

        sut.markChannelsAsDelivered(channels = listOf(channel))

        fixture.verifyUpsertMessageReceiptsCalled(never())
    }

    @Test
    fun `should skip storing channel delivery receipts when last message is already delivered`() = runTest {
        val channel = DeliverableChannel.copy(
            read = listOf(
                randomChannelUserRead(
                    user = CurrentUser,
                    lastRead = NEVER,
                    lastDeliveredAt = Now,
                ),
            ),
        )
        val fixture = Fixture()
        val sut = fixture.get()

        sut.markChannelsAsDelivered(channels = listOf(channel))

        fixture.verifyUpsertMessageReceiptsCalled(never())
    }

    @Test
    fun `should skip storing channel delivery receipts with empty list`() = runTest {
        val fixture = Fixture()
        val sut = fixture.get()

        sut.markChannelsAsDelivered(channels = emptyList())

        fixture.verifyUpsertMessageReceiptsCalled(never())
    }

    private class Fixture {
        private val mockRepositoryFacade = mock<RepositoryFacade> {
            onBlocking { selectUser("me") } doReturn CurrentUser
            onBlocking { selectChannel(DeliverableChannel.cid) } doReturn DeliverableChannel
            onBlocking { selectMessage(DeliverableMessage.id) } doReturn DeliverableMessage
        }
        private val mockMessageReceiptRepository = mock<MessageReceiptRepository>()
        private val mockChatApi = mock<ChatApi> {
            on {
                queryChannel(
                    channelType = any(),
                    channelId = any(),
                    query = any(),
                )
            } doReturn DeliverableChannel.asCall()
            on { getMessage(messageId = DeliverableMessage.id) } doReturn DeliverableMessage.asCall()
        }

        fun givenCurrentUser(user: User?) = apply {
            wheneverBlocking { mockRepositoryFacade.selectUser("me") } doReturn user
        }

        fun givenChannelNotFoundFromRepository() = apply {
            wheneverBlocking { mockRepositoryFacade.selectChannel(cid = any()) } doReturn null
        }

        fun givenMessageNotFoundFromRepository() = apply {
            wheneverBlocking { mockRepositoryFacade.selectMessage(messageId = any()) } doReturn null
        }

        fun givenChannelNotFoundFromApi() = apply {
            wheneverBlocking {
                mockChatApi.queryChannel(
                    channelType = any(),
                    channelId = any(),
                    query = any(),
                )
            } doReturn mock<Error>().asCall()
        }

        fun givenMessageNotFoundFromApi() = apply {
            wheneverBlocking { mockChatApi.getMessage(messageId = any()) } doReturn mock<Error>().asCall()
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
            now = { Now },
            getRepositoryFacade = { mockRepositoryFacade },
            messageReceiptRepository = mockMessageReceiptRepository,
            api = mockChatApi,
        )
    }
}

private val Now = Date()

private val CurrentUser = randomUser(
    privacySettings = PrivacySettings(
        deliveryReceipts = DeliveryReceipts(enabled = true),
    ),
)

private val DeliverableMessage = randomMessage(
    createdLocallyAt = Now,
    deletedAt = null,
    deletedForMe = false,
)

private val DeliverableChannel = randomChannel(
    messages = listOf(DeliverableMessage),
    read = listOf(
        randomChannelUserRead(
            user = CurrentUser,
            lastRead = NEVER,
            lastDeliveredAt = null,
        ),
    ),
)
