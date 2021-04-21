package io.getstream.chat.android.offline.channel.controller

import android.webkit.MimeTypeMap
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.anyOrNull
import com.nhaarman.mockitokotlin2.argThat
import com.nhaarman.mockitokotlin2.doAnswer
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.eq
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.same
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.call.Call
import io.getstream.chat.android.client.channel.ChannelClient
import io.getstream.chat.android.client.errors.ChatError
import io.getstream.chat.android.client.models.Attachment
import io.getstream.chat.android.client.models.Message
import io.getstream.chat.android.client.models.User
import io.getstream.chat.android.client.utils.Result
import io.getstream.chat.android.livedata.randomAttachmentsWithFile
import io.getstream.chat.android.livedata.repository.RepositoryFacade
import io.getstream.chat.android.livedata.utils.DefaultRetryPolicy
import io.getstream.chat.android.livedata.utils.RetryPolicy
import io.getstream.chat.android.offline.ChatDomainImpl
import io.getstream.chat.android.offline.channel.ChannelController
import io.getstream.chat.android.test.TestCall
import io.getstream.chat.android.test.TestCoroutineRule
import io.getstream.chat.android.test.randomString
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Shadows
import java.io.File

@RunWith(AndroidJUnit4::class)
internal class WhenSendMessage {

    @get:Rule
    val testCoroutines = TestCoroutineRule()

    private val scope = testCoroutines.scope

    private val channelClient: ChannelClient = mock {
        on(it.sendMessage(any())) doReturn TestCall(Result(Message()))
    }

    private val chatClient: ChatClient = mock {
        on(it.channel(any(), any())) doReturn channelClient
    }

    private val channelType: String = randomString()
    private val channelId: String = randomString()
    private lateinit var channelController: ChannelController

    private val repos: RepositoryFacade = mock()

    private val doNotRetryPolicy: RetryPolicy = DefaultRetryPolicy()

    private val domainImpl: ChatDomainImpl = mock {
        on(it.scope) doReturn testCoroutines.scope
        on(it.generateMessageId()) doReturn randomString()
        on(it.currentUser) doReturn User()
        on(it.repos) doReturn repos
        on(it.isOnline()) doReturn true
        on(it.getActiveQueries()) doReturn emptyList()
        on(it.retryPolicy) doReturn doNotRetryPolicy
        on(it.client) doReturn chatClient
    }

    @Before
    fun setup() {
        Shadows.shadowOf(MimeTypeMap.getSingleton())
        channelController = ChannelController(channelType, channelId, chatClient, domainImpl)
    }

    @Test
    fun `Message with failed attachment upload should be upload send the right state`() = scope.runBlockingTest {
        whenever(domainImpl.runAndRetry<Message>(any())) doAnswer {
            (it.arguments[0] as () -> Call<Message>).invoke().execute()
        }

        val attachments = randomAttachmentsWithFile().toMutableList()
        val files: List<File> = attachments.map { it.upload!! }

        mockFileUploadsFailure(files)

        channelController.sendMessage(Message(attachments = attachments))

        verify(channelClient).sendMessage(
            argThat { message ->
                message.attachments.all { attach ->
                    attach.uploadState is Attachment.UploadState.Failed
                }
            }
        )
    }

    @Test
    fun `Attachments should be sent with success as uploadState when request success`() = scope.runBlockingTest {
        whenever(domainImpl.runAndRetry<Message>(any())) doAnswer {
            (it.arguments[0] as () -> Call<Message>).invoke().execute()
        }

        val attachments = randomAttachmentsWithFile().toMutableList()
        val files: List<File> = attachments.map { it.upload!! }

        mockFileUploadsSuccess(files)

        channelController.sendMessage(Message(attachments = attachments))

        verify(channelClient).sendMessage(
            argThat { message ->
                message.attachments.all { attach ->
                    attach.uploadState is Attachment.UploadState.Success
                }
            }
        )
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
            val result = Result<String>("file")
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
