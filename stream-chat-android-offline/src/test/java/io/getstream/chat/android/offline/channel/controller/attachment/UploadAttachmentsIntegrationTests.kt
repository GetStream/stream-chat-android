package io.getstream.chat.android.offline.channel.controller.attachment

import android.webkit.MimeTypeMap
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.anyOrNull
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.eq
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.same
import com.nhaarman.mockitokotlin2.whenever
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.channel.ChannelClient
import io.getstream.chat.android.client.errors.ChatError
import io.getstream.chat.android.client.models.Attachment
import io.getstream.chat.android.client.models.Message
import io.getstream.chat.android.client.models.User
import io.getstream.chat.android.client.utils.Result
import io.getstream.chat.android.offline.ChatDomainImpl
import io.getstream.chat.android.offline.channel.ChannelController
import io.getstream.chat.android.offline.integration.BaseRepositoryFacadeIntegrationTest
import io.getstream.chat.android.offline.randomAttachmentsWithFile
import io.getstream.chat.android.offline.randomMessage
import io.getstream.chat.android.test.TestCall
import io.getstream.chat.android.test.randomString
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Shadows
import java.io.File

@RunWith(AndroidJUnit4::class)
internal class UploadAttachmentsIntegrationTests : BaseRepositoryFacadeIntegrationTest() {

    private val channelClient: ChannelClient = mock {
        on(it.sendMessage(any())) doReturn TestCall(Result(Message()))
    }

    private val chatClient: ChatClient = mock {
        on(it.channel(any(), any())) doReturn channelClient
        on(it.channel(any())) doReturn channelClient
    }

    private val channelType: String = randomString()
    private val channelId: String = randomString()
    private lateinit var channelController: ChannelController

    private lateinit var domainImpl: ChatDomainImpl

    @Before
    override fun setup() {
        super.setup()
        Shadows.shadowOf(MimeTypeMap.getSingleton())

        domainImpl = mock {
            on(it.appContext) doReturn mock()
            on(it.scope) doReturn testCoroutines.scope
            on(it.generateMessageId()) doReturn randomString()
            on(it.currentUser) doReturn User()
            on(it.repos) doReturn repositoryFacade
            on(it.isOnline()) doReturn true
            on(it.getActiveQueries()) doReturn emptyList()
            on(it.client) doReturn chatClient
        }

        channelController = ChannelController(channelType, channelId, chatClient, domainImpl)
    }

    @Test
    fun `Given a message with attachments When upload fails Should store the correct upload state`() = runBlocking {
        val attachments = randomAttachmentsWithFile().toMutableList()
        val files: List<File> = attachments.map { it.upload!! }
        val message = randomMessage(attachments = attachments)
        mockFileUploadsFailure(files)

        channelController.uploadAttachments(message)

        val persistedMessage = repositoryFacade.selectMessage(message.id)!!
        Truth.assertThat(persistedMessage.attachments.size).isEqualTo(attachments.size)
        Truth.assertThat(
            persistedMessage.attachments.all { it.uploadState is Attachment.UploadState.Failed }
        ).isTrue()
    }

    @Test
    fun `Given a message with attachments When upload succeeds Should store the correct upload state`() = runBlocking {
        val attachments = randomAttachmentsWithFile().toMutableList()
        val files: List<File> = attachments.map { it.upload!! }
        val message = randomMessage(attachments = attachments)
        mockFileUploadsSuccess(files)

        channelController.uploadAttachments(message)

        val persistedMessage = repositoryFacade.selectMessage(message.id)!!
        Truth.assertThat(persistedMessage.attachments.size).isEqualTo(attachments.size)
        Truth.assertThat(
            persistedMessage.attachments.all { it.uploadState == Attachment.UploadState.Success }
        ).isTrue()
    }

    private fun mockFileUploadsFailure(files: List<File>) {
        for (file in files) {
            val result = Result<String>(ChatError())
            whenever(
                chatClient.sendFile(
                    eq(channelController.channelType),
                    eq(channelController.channelId),
                    same(file),
                    anyOrNull(),
                )
            ) doReturn TestCall(result)
            whenever(
                chatClient.sendImage(
                    eq(channelController.channelType),
                    eq(channelController.channelId),
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
                    eq(channelController.channelType),
                    eq(channelController.channelId),
                    same(file),
                    anyOrNull(),
                )
            ) doReturn TestCall(result)
            whenever(
                chatClient.sendImage(
                    eq(channelController.channelType),
                    eq(channelController.channelId),
                    same(file),
                    anyOrNull(),
                )
            ) doReturn TestCall(result)
        }
    }
}
