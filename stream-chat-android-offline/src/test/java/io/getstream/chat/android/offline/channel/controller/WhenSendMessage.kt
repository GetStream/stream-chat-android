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
import io.getstream.chat.android.client.utils.Result
import io.getstream.chat.android.core.ExperimentalStreamChatApi
import io.getstream.chat.android.offline.ChatDomainImpl
import io.getstream.chat.android.offline.channel.ChannelController
import io.getstream.chat.android.offline.experimental.channel.logic.ChannelLogic
import io.getstream.chat.android.offline.experimental.channel.state.ChannelMutableState
import io.getstream.chat.android.offline.randomAttachmentsWithFile
import io.getstream.chat.android.offline.randomUser
import io.getstream.chat.android.offline.repository.RepositoryFacade
import io.getstream.chat.android.offline.utils.DefaultRetryPolicy
import io.getstream.chat.android.offline.utils.RetryPolicy
import io.getstream.chat.android.test.TestCall
import io.getstream.chat.android.test.TestCoroutineRule
import io.getstream.chat.android.test.randomString
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Before
import org.junit.Ignore
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
        on(it.channel(any())) doReturn channelClient
    }

    private val channelType: String = randomString()
    private val channelId: String = randomString()
    private lateinit var channelController: ChannelController

    private val repos: RepositoryFacade = mock()

    private val doNotRetryPolicy: RetryPolicy = DefaultRetryPolicy()
    private val userFlow = MutableStateFlow(randomUser())

    private val domainImpl: ChatDomainImpl = mock {
        on(it.appContext) doReturn mock()
        on(it.scope) doReturn testCoroutines.scope
        on(it.generateMessageId()) doReturn randomString()
        on(it.user) doReturn userFlow
        on(it.repos) doReturn repos
        on(it.isOnline()) doReturn true
        on(it.getActiveQueries()) doReturn emptyList()
        on(it.retryPolicy) doReturn doNotRetryPolicy
        on(it.client) doReturn chatClient
    }

    @OptIn(ExperimentalStreamChatApi::class)
    @Before
    fun setup() {
        Shadows.shadowOf(MimeTypeMap.getSingleton())
        val mutableState = ChannelMutableState(channelType, channelId, scope, userFlow, MutableStateFlow(emptyMap()))
        channelController = ChannelController(mutableState, ChannelLogic(mutableState, domainImpl), chatClient, domainImpl)
    }

    @Test
    @Ignore("Need to do something with MessageSendingService for this to work")
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
