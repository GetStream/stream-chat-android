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

package io.getstream.chat.android.client.receipts

import io.getstream.chat.android.DeliveryReceipts
import io.getstream.chat.android.PrivacySettings
import io.getstream.chat.android.client.api.ChatApi
import io.getstream.chat.android.client.api.models.QueryChannelRequest
import io.getstream.chat.android.client.extensions.internal.NEVER
import io.getstream.chat.android.client.persistance.repository.RepositoryFacade
import io.getstream.chat.android.client.persistence.repository.MessageReceiptRepository
import io.getstream.chat.android.models.Channel
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
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
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

        val result = sut.markMessageAsDelivered(message)

        val receipts = listOf(
            MessageReceipt(
                messageId = message.id,
                cid = message.cid,
                createdAt = Now,
            ),
        )
        fixture.verifyUpsertMessageReceiptsCalled(receipts = receipts)
        assertTrue(result)
    }

    @Test
    fun `fetch channel from API when channel is not found from repository`() = runTest {
        val message = DeliverableMessage
        val fixture = Fixture()
            .givenChannelNotFoundFromRepository()
        val sut = fixture.get()

        val result = sut.markMessageAsDelivered(message)

        val receipts = listOf(
            MessageReceipt(
                messageId = message.id,
                cid = message.cid,
                createdAt = Now,
            ),
        )
        fixture.verifyUpsertMessageReceiptsCalled(receipts = receipts)
        assertTrue(result)
    }

    @Test
    fun `use the channel held in memory when channel is not found from repository`() = runTest {
        val message = DeliverableMessage
        val fixture = Fixture()
            .givenChannelNotFoundFromRepository()
            .givenActiveChannel(DeliverableChannel)
        val sut = fixture.get()

        val result = sut.markMessageAsDelivered(message)

        val receipts = listOf(
            MessageReceipt(
                messageId = message.id,
                cid = message.cid,
                createdAt = Now,
            ),
        )
        fixture.verifyUpsertMessageReceiptsCalled(receipts = receipts)
        fixture.verifyQueryChannelCalled(never())
        assertTrue(result)
    }

    @Test
    fun `skip without fetching when the loaded channel held in memory has no read for the current user`() = runTest {
        val fixture = Fixture()
            .givenChannelNotFoundFromRepository()
            .givenActiveChannel(DeliverableChannel.copy(read = emptyList()))
        val sut = fixture.get()

        val result = sut.markMessageAsDelivered(DeliverableMessage)

        fixture.verifyQueryChannelCalled(never())
        fixture.verifyUpsertMessageReceiptsCalled(never())
        assertFalse(result)
    }

    @Test
    fun `fetch channel from API when the channel held in memory is not loaded`() = runTest {
        val fixture = Fixture()
            .givenChannelNotFoundFromRepository()
            .givenActiveChannel(
                DeliverableChannel.copy(config = DeliverableChannel.config.copy(createdAt = null)),
            )
        val sut = fixture.get()

        val result = sut.markMessageAsDelivered(DeliverableMessage)

        fixture.verifyQueryChannelCalled()
        fixture.verifyUpsertMessageReceiptsCalled()
        assertTrue(result)
    }

    @Test
    fun `fetch the channel state without messages or watching when channel is not found from repository`() = runTest {
        val fixture = Fixture()
            .givenChannelNotFoundFromRepository()
        val sut = fixture.get()

        sut.markMessageAsDelivered(DeliverableMessage)

        val request = fixture.captureQueryChannelRequest()
        assertTrue(request.state)
        assertFalse(request.watch)
        assertFalse(request.presence)
        assertEquals(mapOf<String, Any>("limit" to 0), request.messages)
        assertEquals(mapOf<String, Any>("limit" to 1, "offset" to 0), request.members)
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

        val result = sut.markMessageAsDelivered(messageId = message.id)

        fixture.verifyUpsertMessageReceiptsCalled(never())
        assertFalse(result)
    }

    @Test
    fun `should skip storing message delivery receipt when channel is not found`() = runTest {
        val message = DeliverableMessage
        val fixture = Fixture()
            .givenChannelNotFoundFromRepository()
            .givenChannelNotFoundFromApi()
        val sut = fixture.get()

        val result = sut.markMessageAsDelivered(message)

        fixture.verifyUpsertMessageReceiptsCalled(never())
        assertFalse(result)
    }

    @Test
    fun `should skip storing message delivery receipt when current user is null`() = runTest {
        val message = DeliverableMessage
        val fixture = Fixture().givenCurrentUser(user = null)
        val sut = fixture.get()

        val result = sut.markMessageAsDelivered(message)

        fixture.verifyUpsertMessageReceiptsCalled(never())
        assertFalse(result)
    }

    @Test
    fun `store message delivery receipt for the confirmed user when the repository has no current user`() = runTest {
        val message = DeliverableMessage
        val fixture = Fixture()
            .givenCurrentUser(user = null)
            .givenConfirmedUser(CurrentUser)
        val sut = fixture.get()

        val result = sut.markMessageAsDelivered(message)

        val receipts = listOf(
            MessageReceipt(
                messageId = message.id,
                cid = message.cid,
                createdAt = Now,
            ),
        )
        fixture.verifyUpsertMessageReceiptsCalled(receipts = receipts)
        assertTrue(result)
    }

    @Test
    fun `should skip storing message delivery receipt when the confirmed user disabled delivery receipts`() = runTest {
        val fixture = Fixture()
            .givenCurrentUser(user = null)
            .givenConfirmedUser(CurrentUser.copy(privacySettings = DeliveryReceiptsDisabled))
        val sut = fixture.get()

        val result = sut.markMessageAsDelivered(DeliverableMessage)

        fixture.verifyUpsertMessageReceiptsCalled(never())
        assertFalse(result)
    }

    @Test
    fun `prefer the repository current user over the confirmed user`() = runTest {
        val fixture = Fixture()
            .givenCurrentUser(CurrentUser.copy(privacySettings = DeliveryReceiptsDisabled))
            .givenConfirmedUser(CurrentUser)
        val sut = fixture.get()

        val result = sut.markMessageAsDelivered(DeliverableMessage)

        fixture.verifyUpsertMessageReceiptsCalled(never())
        assertFalse(result)
    }

    @Test
    fun `should store message delivery receipt when current user privacy settings are undefined`() = runTest {
        val currentUser = CurrentUser.copy(privacySettings = null)
        val message = DeliverableMessage
        val fixture = Fixture().givenCurrentUser(currentUser)
        val sut = fixture.get()

        val result = sut.markMessageAsDelivered(message)

        val receipts = listOf(
            MessageReceipt(
                messageId = message.id,
                cid = message.cid,
                createdAt = Now,
            ),
        )
        fixture.verifyUpsertMessageReceiptsCalled(receipts = receipts)
        assertTrue(result)
    }

    @Test
    fun `should skip storing message delivery receipt when delivery receipts are disabled`() = runTest {
        val currentUser = CurrentUser.copy(privacySettings = DeliveryReceiptsDisabled)
        val message = DeliverableMessage
        val fixture = Fixture().givenCurrentUser(currentUser)
        val sut = fixture.get()

        val result = sut.markMessageAsDelivered(message)

        fixture.verifyUpsertMessageReceiptsCalled(never())
        assertFalse(result)
    }

    @Test
    fun `should skip storing message delivery receipt from the current user`() = runTest {
        val message = DeliverableMessage.copy(user = CurrentUser)
        val fixture = Fixture()
        val sut = fixture.get()

        val result = sut.markMessageAsDelivered(message)

        fixture.verifyUpsertMessageReceiptsCalled(never())
        assertFalse(result)
    }

    @Test
    fun `should skip storing message delivery receipt from shadow banned messages`() = runTest {
        val message = DeliverableMessage.copy(shadowed = true)
        val fixture = Fixture()
        val sut = fixture.get()

        val result = sut.markMessageAsDelivered(message)

        fixture.verifyUpsertMessageReceiptsCalled(never())
        assertFalse(result)
    }

    @Test
    fun `should skip storing message delivery receipt for thread replies not sent to channel`() = runTest {
        val message = DeliverableMessage.copy(
            parentId = "parentId",
            showInChannel = false,
        )
        val fixture = Fixture()
        val sut = fixture.get()

        val result = sut.markMessageAsDelivered(message)

        fixture.verifyUpsertMessageReceiptsCalled(never())
        assertFalse(result)
    }

    @Test
    fun `should store message delivery receipt for thread replies sent to channel`() = runTest {
        val message = DeliverableMessage.copy(
            parentId = "parentId",
            showInChannel = true,
        )
        val fixture = Fixture()
        val sut = fixture.get()

        val result = sut.markMessageAsDelivered(message)

        val receipts = listOf(
            MessageReceipt(
                messageId = message.id,
                cid = message.cid,
                createdAt = Now,
            ),
        )
        fixture.verifyUpsertMessageReceiptsCalled(receipts = receipts)
        assertTrue(result)
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

        val result = sut.markMessageAsDelivered(message)

        fixture.verifyUpsertMessageReceiptsCalled(never())
        assertFalse(result)
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
    fun `store channel delivery receipts for the confirmed user when the repository has no current user`() = runTest {
        val message = DeliverableMessage
        val fixture = Fixture()
            .givenCurrentUser(user = null)
            .givenConfirmedUser(CurrentUser)
        val sut = fixture.get()

        sut.markChannelsAsDelivered(channels = listOf(DeliverableChannel))

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

    @Test
    fun `should propagate failure when persisting receipts fails`() = runTest {
        val fixture = Fixture().givenUpsertFails()
        val sut = fixture.get()

        val failure = runCatching { sut.markMessageAsDelivered(DeliverableMessage) }

        assertTrue(failure.exceptionOrNull() is IllegalStateException)
        fixture.verifyUpsertMessageReceiptsCalled()
    }

    private class Fixture {
        private val messageReceiptRepository: MessageReceiptRepository = mock()

        private val mockRepositoryFacade = mock<RepositoryFacade> {
            onBlocking { selectUser("me") } doReturn CurrentUser
            onBlocking { selectChannel(DeliverableChannel.cid) } doReturn DeliverableChannel
            onBlocking { selectMessage(DeliverableMessage.id) } doReturn DeliverableMessage
        }
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

        private var confirmedUser: User? = null

        fun givenCurrentUser(user: User?) = apply {
            wheneverBlocking { mockRepositoryFacade.selectUser("me") } doReturn user
        }

        fun givenConfirmedUser(user: User) = apply {
            confirmedUser = user
        }

        private var activeChannel: Channel? = null

        fun givenActiveChannel(channel: Channel) = apply {
            activeChannel = channel
        }

        fun verifyQueryChannelCalled(mode: VerificationMode = times(1)) {
            verify(mockChatApi, mode).queryChannel(
                channelType = any(),
                channelId = any(),
                query = any(),
            )
        }

        fun givenUpsertFails() = apply {
            wheneverBlocking { messageReceiptRepository.upsertMessageReceipts(any()) }.thenAnswer {
                throw IllegalStateException("upsert failed")
            }
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

        fun captureQueryChannelRequest(): QueryChannelRequest {
            val captor = argumentCaptor<QueryChannelRequest>()
            verify(mockChatApi).queryChannel(
                channelType = any(),
                channelId = any(),
                query = captor.capture(),
            )
            return captor.firstValue
        }

        fun verifyUpsertMessageReceiptsCalled(
            mode: VerificationMode = times(1),
            receipts: List<MessageReceipt>? = null,
        ) {
            verifyBlocking(messageReceiptRepository, mode) {
                upsertMessageReceipts(receipts ?: any())
            }
        }

        fun get() = MessageReceiptManager(
            now = { Now },
            getRepositoryFacade = { mockRepositoryFacade },
            getConfirmedUser = { confirmedUser },
            getActiveChannel = { cid -> activeChannel?.takeIf { cid == DeliverableMessage.cid } },
            messageReceiptRepository = messageReceiptRepository,
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

private val DeliveryReceiptsDisabled = PrivacySettings(
    deliveryReceipts = DeliveryReceipts(enabled = false),
)

private val DeliverableMessage = randomMessage(
    createdLocallyAt = Now,
    deletedAt = null,
    deletedForMe = false,
    parentId = null,
)

private val DeliverableChannel = randomChannel(
    config = randomConfig(deliveryEventsEnabled = true),
    messages = listOf(DeliverableMessage),
    read = listOf(
        randomChannelUserRead(
            user = CurrentUser,
            lastRead = NEVER,
            lastDeliveredAt = null,
        ),
    ),
)
