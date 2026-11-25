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

package io.getstream.chat.android.compose.ui.scheduled

import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performSemanticsAction
import androidx.compose.ui.test.performTextInput
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import androidx.test.ext.junit.runners.AndroidJUnit4
import io.getstream.chat.android.client.persistance.repository.RepositoryFacade
import io.getstream.chat.android.client.test.MockedChatClientTest
import io.getstream.chat.android.compose.ui.theme.ChatTheme
import io.getstream.chat.android.compose.viewmodel.messages.MessageComposerViewModel
import io.getstream.chat.android.compose.viewmodel.scheduled.ScheduledMessageListViewModelFactory
import io.getstream.chat.android.models.App
import io.getstream.chat.android.models.AppSettings
import io.getstream.chat.android.models.Attachment
import io.getstream.chat.android.models.Channel
import io.getstream.chat.android.models.ChannelInfo
import io.getstream.chat.android.models.ConnectionState
import io.getstream.chat.android.models.FileUploadConfig
import io.getstream.chat.android.models.Message
import io.getstream.chat.android.models.QueryScheduledMessagesResult
import io.getstream.chat.android.models.ScheduledMessage
import io.getstream.chat.android.models.ScheduledMessageFailure
import io.getstream.chat.android.models.ScheduledMessageStatus
import io.getstream.chat.android.models.User
import io.getstream.chat.android.test.asCall
import io.getstream.result.Error
import io.getstream.result.Result
import io.getstream.result.call.CoroutineCall
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.emptyFlow
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.any
import org.mockito.kotlin.anyOrNull
import org.mockito.kotlin.argThat
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.eq
import org.mockito.kotlin.isNull
import org.mockito.kotlin.mock
import org.mockito.kotlin.mockingDetails
import org.mockito.kotlin.never
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.robolectric.annotation.Config
import java.util.Date
import java.util.concurrent.TimeUnit

@RunWith(AndroidJUnit4::class)
@Config(sdk = [33])
internal class ScheduledMessageEditTest : MockedChatClientTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val channel = Channel(type = "messaging", id = "general", name = "General")
    private val scheduledMessagesFlow = MutableStateFlow<List<ScheduledMessage>>(emptyList())

    @Before
    fun setUp() {
        whenever(mockClientState.user) doReturn MutableStateFlow<User?>(User(id = "me"))
        whenever(mockClientState.connectionState) doReturn MutableStateFlow(ConnectionState.Connected)
        whenever(mockChatClient.scheduledMessages) doReturn scheduledMessagesFlow
        whenever(mockChatClient.hasLoadedAllScheduledMessages) doReturn MutableStateFlow(true)
        whenever(mockChatClient.deletedScheduledMessagesAlreadySent) doReturn emptyFlow()
        whenever(mockChatClient.queryScheduledMessages(anyOrNull(), any(), any(), anyOrNull())) doReturn
            QueryScheduledMessagesResult(emptyList(), next = null).asCall()
        whenever(mockChatClient.queryUsers(any())) doReturn emptyList<User>().asCall()
        val repository: RepositoryFacade = mock { onBlocking { selectUsers(any()) } doReturn emptyList() }
        whenever(mockChatClient.repositoryFacade) doReturn repository
        val uploadConfig = FileUploadConfig(
            allowedFileExtensions = emptyList(),
            allowedMimeTypes = emptyList(),
            blockedFileExtensions = emptyList(),
            blockedMimeTypes = emptyList(),
            sizeLimitInBytes = AppSettings.DEFAULT_SIZE_LIMIT_IN_BYTES,
        )
        whenever(mockChatClient.getAppSettings()) doReturn
            AppSettings(app = App(name = "app", fileUploadConfig = uploadConfig, imageUploadConfig = uploadConfig))
    }

    @Test
    fun `edit opens a composer below the list, with the message, named as a scheduled message`() {
        scheduledMessagesFlow.value = listOf(scheduled(id = "a", text = "Heads up"))
        setContent()
        composeTestRule.onNodeWithTag("Stream_ComposerInputField").assertDoesNotExist()

        startEditing("Heads up")

        composeTestRule.onNodeWithText("Edit scheduled message").assertExists()
        composeTestRule.onNodeWithTag("Stream_ComposerInputField").assertTextEquals("Heads up")
        composeTestRule.onNodeWithTag("Stream_ScheduledMessageList").assertExists()
    }

    @Test
    @Config(sdk = [33], qualifiers = "w411dp-h1600dp")
    fun `editing another message replaces the content of the composer`() {
        scheduledMessagesFlow.value = listOf(
            scheduled(id = "a", text = "First message"),
            scheduled(id = "b", text = "Second message"),
        )
        setContent()

        startEditing("First message")
        startEditing("Second message")

        composeTestRule.onNodeWithTag("Stream_ComposerInputField").assertTextEquals("Second message")
        composeTestRule.onNodeWithTag("Stream_EditMessagePreview", useUnmergedTree = true)
            .assertTextEquals("Second message")
    }

    @Test
    @Config(sdk = [33], qualifiers = "w411dp-h1600dp")
    fun `saving after editing another message saves that message only`() {
        val second = scheduled(id = "b", text = "Second message")
        scheduledMessagesFlow.value = listOf(scheduled(id = "a", text = "First message"), second)
        whenever(mockChatClient.updateScheduledMessage(any(), any(), anyOrNull())) doReturn second.asCall()
        setContent()
        startEditing("First message")
        startEditing("Second message")

        save()
        composeTestRule.waitUntil { mockingDetails(mockChatClient).invocations.any { it.method.name == "updateScheduledMessage" } }

        verify(mockChatClient).updateScheduledMessage(eq("b"), argThat { text == "Second message" }, isNull())
        verify(mockChatClient, never()).updateScheduledMessage(eq("a"), any(), anyOrNull())
        composeTestRule.onNodeWithText("Edit scheduled message").assertDoesNotExist()
    }

    @Test
    @Config(sdk = [33], qualifiers = "w411dp-h1600dp")
    fun `an attachment picked for one message is not saved with the next message edited`() {
        val second = scheduled(id = "b", text = "Second message")
        scheduledMessagesFlow.value = listOf(scheduled(id = "a", text = "First message"), second)
        whenever(mockChatClient.updateScheduledMessage(any(), any(), anyOrNull())) doReturn second.asCall()
        setContent()
        startEditing("First message")
        // The composer of the channel, shared by the edits of its messages.
        val composerViewModel = ViewModelProvider(viewModelStoreOwner)[
            "Stream_ScheduledMessageEdit_${channel.cid}_null_composer",
            MessageComposerViewModel::class.java,
        ]
        composeTestRule.runOnIdle { composerViewModel.addAttachments(listOf(Attachment(name = "picked.png"))) }
        startEditing("Second message")

        save()
        composeTestRule.waitUntil { mockingDetails(mockChatClient).invocations.any { it.method.name == "updateScheduledMessage" } }

        verify(mockChatClient).updateScheduledMessage(eq("b"), argThat { attachments.isEmpty() }, isNull())
    }

    @Test
    fun `saving after the quote is removed saves the message without it`() {
        val quoted = Message(id = "quoted", text = "Original", deletedAt = Date())
        val message = scheduled(id = "a", text = "Heads up", replyTo = quoted)
        scheduledMessagesFlow.value = listOf(message)
        whenever(mockChatClient.updateScheduledMessage(any(), any(), anyOrNull())) doReturn message.asCall()
        setContent()
        startEditing("Heads up")
        clickTag("Stream_ScheduledMessageRemoveQuote")

        save()

        verify(mockChatClient).updateScheduledMessage(eq("a"), argThat { replyTo == null && replyMessageId == null }, isNull())
    }

    @Test
    fun `a save that ends after the message was reopened leaves the new edit open`() {
        val message = scheduled(id = "a", text = "Heads up")
        scheduledMessagesFlow.value = listOf(message)
        val pending = CompletableDeferred<Result<ScheduledMessage>>()
        whenever(mockChatClient.updateScheduledMessage(any(), any(), anyOrNull())) doReturn
            CoroutineCall(CoroutineScope(Dispatchers.Main)) { pending.await() }
        setContent()
        startEditing("Heads up")
        save()
        clickTag("Stream_ComposerCancelEditButton")
        startEditing("Heads up")

        pending.complete(Result.Success(message))
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithText("Edit scheduled message").assertExists()
        composeTestRule.onNodeWithTag("Stream_ComposerInputField").assertTextEquals("Heads up")
    }

    @Test
    fun `a message being saved can't be saved again after it is reopened, until the save fails`() {
        val message = scheduled(id = "a", text = "Heads up")
        scheduledMessagesFlow.value = listOf(message)
        val first = CompletableDeferred<Result<ScheduledMessage>>()
        whenever(mockChatClient.updateScheduledMessage(any(), any(), anyOrNull()))
            .doReturn(CoroutineCall(CoroutineScope(Dispatchers.Main)) { first.await() })
            .doReturn(message.asCall())
        setContent()
        startEditing("Heads up")
        save()
        clickTag("Stream_ComposerCancelEditButton")
        startEditing("Heads up")

        save()
        verify(mockChatClient, times(1)).updateScheduledMessage(any(), any(), anyOrNull())

        first.complete(Result.Failure(Error.GenericError("Refused")))
        composeTestRule.waitForIdle()
        save()
        verify(mockChatClient, times(2)).updateScheduledMessage(any(), any(), anyOrNull())
    }

    @Test
    fun `a failed message being scheduled again can't be scheduled twice, until scheduling fails`() {
        val failed = scheduled(id = "a", text = "Heads up").copy(
            status = ScheduledMessageStatus.Failed,
            failure = ScheduledMessageFailure.ServerFailedPublishing(reason = null),
        )
        scheduledMessagesFlow.value = listOf(failed)
        val first = CompletableDeferred<Result<ScheduledMessage>>()
        whenever(mockChatClient.scheduleMessage(any(), any(), any(), any()))
            .doReturn(CoroutineCall(CoroutineScope(Dispatchers.Main)) { first.await() })
            .doReturn(failed.asCall())
        whenever(mockChatClient.deleteScheduledMessage(any())) doReturn Unit.asCall()
        setContent()
        startEditing("Heads up", action = "Stream_ScheduledMessageAction_EditAndReschedule")
        save()
        clickTag("Stream_ScheduleMessageConfirm")
        clickTag("Stream_ComposerCancelEditButton")
        startEditing("Heads up", action = "Stream_ScheduledMessageAction_EditAndReschedule")

        save()
        composeTestRule.onNodeWithTag("Stream_ScheduleMessageTimeSheet").assertDoesNotExist()
        verify(mockChatClient, times(1)).scheduleMessage(any(), any(), any(), any())

        first.complete(Result.Failure(Error.GenericError("Refused")))
        composeTestRule.waitForIdle()
        save()
        clickTag("Stream_ScheduleMessageConfirm")
        verify(mockChatClient, times(2)).scheduleMessage(any(), any(), any(), any())
    }

    @Test
    fun `a message that quotes a deleted message is not saved until the quote is removed, whatever the button`() {
        val quoted = Message(id = "quoted", text = "Original", deletedAt = Date())
        val message = scheduled(id = "a", text = "Heads up", replyTo = quoted)
        scheduledMessagesFlow.value = listOf(message)
        whenever(mockChatClient.updateScheduledMessage(any(), any(), anyOrNull())) doReturn message.asCall()
        setContent()
        startEditing("Heads up")

        save()
        verify(mockChatClient, never()).updateScheduledMessage(any(), any(), anyOrNull())

        clickTag("Stream_ScheduledMessageRemoveQuote")
        save()
        verify(mockChatClient).updateScheduledMessage(eq("a"), any(), isNull())
    }

    @Test
    fun `cancel closes the composer`() {
        scheduledMessagesFlow.value = listOf(scheduled(id = "a", text = "Heads up"))
        setContent()
        startEditing("Heads up")

        clickTag("Stream_ComposerCancelEditButton")

        composeTestRule.onNodeWithText("Edit scheduled message").assertDoesNotExist()
        composeTestRule.onNodeWithTag("Stream_ComposerInputField").assertDoesNotExist()
    }

    @Test
    fun `edit loads the users the message mentions`() {
        scheduledMessagesFlow.value = listOf(scheduled(id = "a", text = "Hi @Ann", mentionedUsersIds = listOf("ann")))
        setContent()

        startEditing("Hi @Ann")

        verify(mockChatClient).queryUsers(any())
    }

    @Test
    fun `a message sent while it is edited keeps the composer and tells it is gone`() {
        scheduledMessagesFlow.value = listOf(scheduled(id = "a", text = "Heads up"))
        setContent()
        startEditing("Heads up")

        scheduledMessagesFlow.value = emptyList()
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithText("Already sent or deleted on another device").assertExists()
        composeTestRule.onNodeWithTag("Stream_ComposerInputField").assertTextEquals("Heads up")
    }

    @Test
    fun `a message the search hides while it is edited is not gone, and can be saved`() {
        val message = scheduled(id = "a", text = "Heads up")
        scheduledMessagesFlow.value = listOf(message, scheduled(id = "b", text = "Launch day"))
        whenever(mockChatClient.updateScheduledMessage(any(), any(), anyOrNull())) doReturn message.asCall()
        setContent()
        startEditing("Heads up")

        composeTestRule.onNodeWithTag("Stream_ScheduledMessageSearchInput").performTextInput("launch")
        composeTestRule.waitForIdle()

        composeTestRule.onNode(hasTestTag("Stream_ScheduledMessageItem") and hasText("Heads up", substring = true))
            .assertDoesNotExist()
        composeTestRule.onNodeWithText("Already sent or deleted on another device").assertDoesNotExist()
        save()
        verify(mockChatClient).updateScheduledMessage(eq("a"), argThat { text == "Heads up" }, isNull())
    }

    @Test
    fun `a message that starts sending while it is edited tells it is gone`() {
        val message = scheduled(id = "a", text = "Heads up")
        scheduledMessagesFlow.value = listOf(message)
        setContent()
        startEditing("Heads up")

        scheduledMessagesFlow.value = listOf(message.copy(status = ScheduledMessageStatus.Sending))
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithText("Already sent or deleted on another device").assertExists()
    }

    @Test
    fun `a message that quotes a deleted message shows a banner until the quote is removed`() {
        val quoted = Message(id = "quoted", text = "Original", deletedAt = Date())
        scheduledMessagesFlow.value = listOf(scheduled(id = "a", text = "Heads up", replyTo = quoted))
        setContent()
        composeTestRule.onNodeWithText("The quoted message was deleted").assertDoesNotExist()
        startEditing("Heads up")

        composeTestRule.onNodeWithText("The quoted message was deleted").assertExists()
        clickTag("Stream_ScheduledMessageRemoveQuote")

        composeTestRule.onNodeWithText("The quoted message was deleted").assertDoesNotExist()
        composeTestRule.onNodeWithText("Edit scheduled message").assertExists()
    }

    private fun startEditing(text: String, action: String = "Stream_ScheduledMessageAction_Edit") {
        // By semantics, as a row can be partly hidden below the composer of a small test screen.
        composeTestRule
            .onNode(hasTestTag("Stream_ScheduledMessageItem") and hasText(text, substring = true))
            .performSemanticsAction(SemanticsActions.OnClick)
        clickTag(action)
        composeTestRule.waitForIdle()
    }

    /** Taps the check button by its semantics action, as the channel of the mocked client grants no capability. */
    private fun save() {
        clickTag("Stream_ComposerSaveButton")
    }

    private fun clickTag(tag: String) {
        composeTestRule.onNodeWithTag(tag).performSemanticsAction(SemanticsActions.OnClick)
        composeTestRule.waitForIdle()
    }

    private lateinit var viewModelStoreOwner: ViewModelStoreOwner

    private fun setContent() {
        composeTestRule.setContent {
            viewModelStoreOwner = requireNotNull(LocalViewModelStoreOwner.current)
            ChatTheme {
                ScheduledMessagesScreen(
                    viewModelFactory = ScheduledMessageListViewModelFactory(chatClient = mockChatClient),
                )
            }
        }
        composeTestRule.waitForIdle()
    }

    private fun scheduled(
        id: String,
        text: String,
        replyTo: Message? = null,
        mentionedUsersIds: List<String> = emptyList(),
    ): ScheduledMessage {
        val now = System.currentTimeMillis()
        return ScheduledMessage(
            id = id,
            cid = channel.cid,
            channel = ChannelInfo(cid = channel.cid, id = channel.id, type = channel.type, name = channel.name),
            message = Message(
                id = id,
                cid = channel.cid,
                text = text,
                replyTo = replyTo,
                mentionedUsersIds = mentionedUsersIds,
            ),
            scheduledAt = Date(now + TimeUnit.DAYS.toMillis(1)),
            status = ScheduledMessageStatus.Scheduled,
            failure = null,
            createdAt = Date(now),
            updatedAt = Date(now),
        )
    }
}
