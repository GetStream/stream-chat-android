/*
 * Copyright (c) 2014-2022 Stream.io Inc. All rights reserved.
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

package io.getstream.chat.android.offline.channel.controller.attachment

import android.webkit.MimeTypeMap
import androidx.annotation.CallSuper
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.errors.ChatError
import io.getstream.chat.android.client.models.Attachment
import io.getstream.chat.android.client.models.Message
import io.getstream.chat.android.client.persistance.repository.MessageRepository
import io.getstream.chat.android.client.query.pagination.AnyChannelPaginationRequest
import io.getstream.chat.android.client.utils.Result
import io.getstream.chat.android.client.utils.SyncStatus
import io.getstream.chat.android.offline.message.attachments.internal.UploadAttachmentsWorker
import io.getstream.chat.android.offline.plugin.logic.internal.LogicRegistry
import io.getstream.chat.android.offline.randomAttachmentsWithFile
import io.getstream.chat.android.offline.randomMessage
import io.getstream.chat.android.offline.randomUser
import io.getstream.chat.android.test.TestCall
import io.getstream.chat.android.test.TestCoroutineRule
import io.getstream.chat.android.test.randomString
import kotlinx.coroutines.runBlocking
import org.amshove.kluent.shouldBeEqualTo
import org.amshove.kluent.shouldBeTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.any
import org.mockito.kotlin.anyOrNull
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.same
import org.mockito.kotlin.whenever
import org.robolectric.Shadows
import java.io.File
import java.util.Date

@RunWith(AndroidJUnit4::class)
internal class UploadAttachmentsIntegrationTests {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    @get:Rule
    val testCoroutines = TestCoroutineRule()

    private val currentUser = randomUser()
    private val messageRepository = MockMessageRepository()

    private val chatClient: ChatClient = mock {
        on(it.containsStoredCredentials()) doReturn true
    }

    private val channelType: String = randomString()
    private val channelId: String = randomString()
    private lateinit var uploadAttachmentsWorker: UploadAttachmentsWorker

    private lateinit var logicRegistry: LogicRegistry

    @Before
    @CallSuper
    fun setup() {
        Shadows.shadowOf(MimeTypeMap.getSingleton())

        logicRegistry = mock {
            on(it.channel(any(), any())) doReturn mock()
        }

        uploadAttachmentsWorker = UploadAttachmentsWorker(logicRegistry, messageRepository, chatClient)
    }

    @Test
    fun `Given a message with attachments When upload fails Should store the correct upload state`(): Unit =
        runBlocking {
            val attachments = randomAttachmentsWithFile().map {
                it.copy(uploadState = Attachment.UploadState.Idle)
            }.toMutableList()
            val files: List<File> = attachments.map { it.upload!! }
            val message = randomMessage(attachments = attachments)
            mockFileUploadsFailure(files)

            messageRepository.insertMessage(message)

            uploadAttachmentsWorker.uploadAttachmentsForMessage(channelType, channelId, message.id)

            val persistedMessage = messageRepository.selectMessage(message.id)!!
            persistedMessage.attachments.size shouldBeEqualTo attachments.size
            persistedMessage.attachments.all { it.uploadState is Attachment.UploadState.Failed }.shouldBeTrue()
        }

    @Test
    fun `Given a message with attachments When upload succeeds Should store the correct upload state`(): Unit =
        runBlocking {
            val attachments = randomAttachmentsWithFile().map {
                it.copy(uploadState = Attachment.UploadState.Idle)
            }.toMutableList()
            val files: List<File> = attachments.map { it.upload!! }
            val message = randomMessage(attachments = attachments)
            mockFileUploadsSuccess(files)

            messageRepository.insertMessage(message)

            uploadAttachmentsWorker.uploadAttachmentsForMessage(channelType, channelId, message.id)

            val persistedMessage = messageRepository.selectMessage(message.id)!!
            persistedMessage.attachments.size shouldBeEqualTo attachments.size

            persistedMessage.attachments.all { it.uploadState == Attachment.UploadState.Success }.shouldBeTrue()
        }

    private fun mockFileUploadsFailure(files: List<File>) {
        for (file in files) {
            val result = Result<String>(ChatError())
            whenever(
                chatClient.sendFile(
                    eq(channelType),
                    eq(channelId),
                    same(file),
                    anyOrNull(),
                )
            ) doReturn TestCall(result)
            whenever(
                chatClient.sendImage(
                    eq(channelType),
                    eq(channelId),
                    same(file),
                    anyOrNull(),
                )
            ) doReturn TestCall(result)
        }
    }

    private fun mockFileUploadsSuccess(files: List<File>) {
        for (file in files) {
            val result = Result("file")
            whenever(
                chatClient.sendFile(
                    eq(channelType),
                    eq(channelId),
                    any(),
                    anyOrNull(),
                )
            ) doReturn TestCall(result)
            whenever(
                chatClient.sendImage(
                    eq(channelType),
                    eq(channelId),
                    any(),
                    anyOrNull(),
                )
            ) doReturn TestCall(result)
        }
    }
}

internal class MockMessageRepository : MessageRepository {

    private val messages = hashMapOf<String, Message>()

    override suspend fun selectMessagesForChannel(
        cid: String,
        pagination: AnyChannelPaginationRequest?,
    ): List<Message> {
        TODO("Not yet implemented")
    }

    override suspend fun selectMessages(messageIds: List<String>, forceCache: Boolean): List<Message> {
        return messages.filter { (messageId, _) ->
            messageIds.contains(messageId)
        }.values.toList()
    }

    override suspend fun selectMessage(messageId: String): Message? {
        return messages[messageId]
    }

    override suspend fun insertMessages(messages: List<Message>, cache: Boolean) {
        messages.forEach { this.messages[it.id] = it }
    }

    override suspend fun insertMessage(message: Message, cache: Boolean) {
        messages[message.id] = message
    }

    override suspend fun deleteChannelMessagesBefore(cid: String, hideMessagesBefore: Date) {
        TODO("Not yet implemented")
    }

    override suspend fun deleteChannelMessage(message: Message) {
        messages.remove(message.id)
    }

    override suspend fun selectMessageIdsBySyncState(syncStatus: SyncStatus): List<String> {
        TODO("Not yet implemented")
    }

    override suspend fun selectMessageBySyncState(syncStatus: SyncStatus): List<Message> {
        TODO("Not yet implemented")
    }
}
