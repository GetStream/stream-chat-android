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

import android.content.ClipboardManager
import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performSemanticsAction
import androidx.compose.ui.test.performTextInput
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.setup.state.ClientState
import io.getstream.chat.android.compose.R
import io.getstream.chat.android.compose.ui.theme.ChatComponentFactory
import io.getstream.chat.android.compose.ui.theme.ChatTheme
import io.getstream.chat.android.compose.ui.theme.ScheduledMessageActionsSheetParams
import io.getstream.chat.android.compose.viewmodel.scheduled.ScheduledMessageListViewModelFactory
import io.getstream.chat.android.models.Channel
import io.getstream.chat.android.models.ChannelInfo
import io.getstream.chat.android.models.ConnectionState
import io.getstream.chat.android.models.Message
import io.getstream.chat.android.models.QueryScheduledMessagesResult
import io.getstream.chat.android.models.ScheduledMessage
import io.getstream.chat.android.models.ScheduledMessageFailure
import io.getstream.chat.android.models.ScheduledMessageStatus
import io.getstream.chat.android.models.User
import io.getstream.chat.android.test.asCall
import io.getstream.result.Error
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.any
import org.mockito.kotlin.anyOrNull
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.robolectric.annotation.Config
import java.util.Date
import java.util.concurrent.TimeUnit

@RunWith(AndroidJUnit4::class)
@Config(sdk = [33])
internal class ScheduledMessagesScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val channel = Channel(type = "messaging", id = "general", name = "General")
    private lateinit var scheduledMessagesFlow: MutableStateFlow<List<ScheduledMessage>>
    private lateinit var deletedAlreadySent: MutableSharedFlow<ScheduledMessage>
    private lateinit var connection: MutableStateFlow<ConnectionState>
    private lateinit var client: ChatClient

    @Before
    fun setUp() {
        scheduledMessagesFlow = MutableStateFlow(emptyList())
        deletedAlreadySent = MutableSharedFlow(extraBufferCapacity = 1)
        connection = MutableStateFlow(ConnectionState.Connected)
        val state: ClientState = mock {
            on { connectionState } doReturn connection
            on { user } doReturn MutableStateFlow<User?>(User(id = "me"))
        }
        client = mock {
            on { scheduledMessages } doReturn scheduledMessagesFlow
            on { hasLoadedAllScheduledMessages } doReturn MutableStateFlow(true)
            on { deletedScheduledMessagesAlreadySent } doReturn deletedAlreadySent
            on { clientState } doReturn state
            on { queryScheduledMessages(anyOrNull(), any(), any(), anyOrNull()) } doReturn
                QueryScheduledMessagesResult(emptyList(), next = null).asCall()
            on { repositoryFacade } doReturn mock()
        }
    }

    @Test
    fun `the screen shows its title, the messages, and goes back`() {
        scheduledMessagesFlow.value = listOf(scheduled(text = "Hello there"))
        var backs = 0
        setContent(onBackPressed = { backs++ })

        composeTestRule.onNodeWithText("Scheduled messages").assertExists()
        composeTestRule.onNodeWithText("Hello there", substring = true, useUnmergedTree = true).assertExists()
        composeTestRule.onNodeWithTag("Stream_BackButton").performClick()
        assertEquals(1, backs)
    }

    @Test
    fun `the empty screen says there are no scheduled messages`() {
        setContent()

        composeTestRule.onNodeWithText("No scheduled messages").assertExists()
    }

    @Test
    fun `the search keeps the matching messages, and tells when none matches`() {
        scheduledMessagesFlow.value = listOf(
            scheduled(id = "a", text = "Heads up"),
            scheduled(id = "b", text = "Launch day"),
        )
        setContent()

        composeTestRule.onNodeWithTag("Stream_ScheduledMessageSearchInput").performTextInput("launch")
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithText("Launch day", substring = true, useUnmergedTree = true).assertExists()
        composeTestRule.onNodeWithText("Heads up", substring = true, useUnmergedTree = true).assertDoesNotExist()

        composeTestRule.onNodeWithTag("Stream_ScheduledMessageSearchInput").performTextInput(" party")
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithText("No results for \"launch party\"").assertExists()
        composeTestRule.onNodeWithTag("Stream_ScheduledMessageList").assertDoesNotExist()
    }

    @Test
    fun `a custom action shows in the sheet that closes before the action is called`() {
        scheduledMessagesFlow.value = listOf(scheduled())
        val clicked = mutableListOf<String>()
        setContent(
            componentFactory = object : ChatComponentFactory {
                @Composable
                override fun ScheduledMessageActionsSheet(params: ScheduledMessageActionsSheetParams) {
                    super.ScheduledMessageActionsSheet(
                        params.copy(
                            customActions = listOf(
                                ScheduledMessageCustomAction(
                                    title = "Share",
                                    icon = painterResource(R.drawable.stream_design_ic_share),
                                    onClick = { clicked += params.item.scheduledMessage.id },
                                ),
                            ),
                        ),
                    )
                }
            },
        )

        openActions()
        composeTestRule.onNodeWithText("Share").performSemanticsAction(SemanticsActions.OnClick)
        composeTestRule.waitForIdle()

        assertEquals(listOf("scheduled-1"), clicked)
        composeTestRule.onNodeWithTag("Stream_ScheduledMessageActions").assertDoesNotExist()
    }

    @Test
    fun `send now sends the message`() {
        val message = scheduled()
        scheduledMessagesFlow.value = listOf(message)
        whenever(client.sendScheduledMessageNow(message.id)) doReturn message.asCall()
        setContent()

        openActions()
        clickTag("Stream_ScheduledMessageAction_SendNow")
        composeTestRule.waitForIdle()

        verify(client).sendScheduledMessageNow(message.id)
    }

    @Test
    fun `send now while offline tells it is queued until online`() {
        val message = scheduled()
        scheduledMessagesFlow.value = listOf(message)
        whenever(client.sendScheduledMessageNow(message.id)) doReturn message.asCall()
        setContent()
        connection.value = ConnectionState.Offline

        openActions()
        clickTag("Stream_ScheduledMessageAction_SendNow")
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithText("Sends when you're back online").assertExists()
    }

    @Test
    fun `delete asks for confirmation, then deletes the message`() {
        val message = scheduled()
        scheduledMessagesFlow.value = listOf(message)
        whenever(client.deleteScheduledMessage(message.id)) doReturn Unit.asCall()
        setContent()

        openActions()
        clickTag("Stream_ScheduledMessageAction_Delete")
        composeTestRule.onNodeWithText("Delete scheduled message?").assertExists()
        clickTag("Stream_ScheduledMessageDeleteConfirm")
        composeTestRule.waitForIdle()

        verify(client).deleteScheduledMessage(message.id)
    }

    @Test
    fun `deleting a message that was already sent tells it is in the channel`() {
        val message = scheduled()
        scheduledMessagesFlow.value = listOf(message)
        whenever(client.deleteScheduledMessage(message.id)) doReturn Unit.asCall()
        setContent()

        openActions()
        clickTag("Stream_ScheduledMessageAction_Delete")
        clickTag("Stream_ScheduledMessageDeleteConfirm")
        scheduledMessagesFlow.value = emptyList()
        deletedAlreadySent.tryEmit(message)
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithText("Already sent. It's in the channel now.").assertExists()
    }

    @Test
    fun `reschedule opens the time sheet and reschedules at the picked date`() {
        val message = scheduled()
        scheduledMessagesFlow.value = listOf(message)
        whenever(client.rescheduleMessage(eq(message.id), any())) doReturn message.asCall()
        setContent()

        openActions()
        clickTag("Stream_ScheduledMessageAction_Reschedule")
        composeTestRule.onNodeWithText("Reschedule message").assertExists()
        clickTag("Stream_ScheduleMessageConfirm")
        composeTestRule.waitForIdle()

        verify(client).rescheduleMessage(eq(message.id), any())
    }

    @Test
    fun `copy text copies the text of a message whose channel was deleted`() {
        scheduledMessagesFlow.value = listOf(
            scheduled(
                text = "Booking confirmed",
                status = ScheduledMessageStatus.Failed,
                failure = ScheduledMessageFailure.ServerFailedPublishing(
                    ScheduledMessageFailure.ServerFailedPublishing.Reason.ChannelDeleted,
                ),
            ),
        )
        setContent()

        openActions()
        clickTag("Stream_ScheduledMessageAction_CopyText")
        composeTestRule.waitForIdle()

        val clipboard = ApplicationProvider.getApplicationContext<Context>()
            .getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        assertEquals("Booking confirmed", clipboard.primaryClip?.getItemAt(0)?.text)
    }

    @Test
    fun `a tap on a sending message tells it is still sending`() {
        scheduledMessagesFlow.value = listOf(scheduled(status = ScheduledMessageStatus.Sending))
        setContent()

        composeTestRule.onNodeWithTag("Stream_ScheduledMessageItem").performClick()

        composeTestRule.onNodeWithText("Still sending. Try again in a moment.").assertExists()
        composeTestRule.onNodeWithTag("Stream_ScheduledMessageActions").assertDoesNotExist()
    }

    @Test
    fun `a message that leaves the list while its actions are open closes them with a notice`() {
        scheduledMessagesFlow.value = listOf(scheduled())
        setContent()
        openActions()

        scheduledMessagesFlow.value = emptyList()
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithTag("Stream_ScheduledMessageActions").assertDoesNotExist()
        composeTestRule.onNodeWithText("Already sent or deleted on another device").assertExists()
    }

    @Test
    fun `an action that fails tells the user`() {
        val message = scheduled()
        scheduledMessagesFlow.value = listOf(message)
        whenever(client.sendScheduledMessageNow(message.id)) doReturn
            Error.GenericError("Refused").asCall()
        setContent()

        openActions()
        clickTag("Stream_ScheduledMessageAction_SendNow")
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithText("Couldn't change the scheduled message. Try again.").assertExists()
    }

    @Test
    fun `the load error tries again`() {
        connection.value = ConnectionState.Offline
        whenever(client.hasLoadedAllScheduledMessages) doReturn MutableStateFlow(false)
        setContent()

        composeTestRule.onNodeWithText("Couldn't load scheduled messages").assertExists()
        composeTestRule.onNodeWithTag("Stream_ScheduledMessageListRetry").performClick()
        composeTestRule.waitForIdle()

        verify(client).queryScheduledMessages(anyOrNull(), any(), any(), anyOrNull())
    }

    private fun clickTag(tag: String) {
        composeTestRule.onNodeWithTag(tag).performSemanticsAction(SemanticsActions.OnClick)
    }

    private fun openActions() {
        composeTestRule.onNodeWithTag("Stream_ScheduledMessageItem").performClick()
        composeTestRule.onNodeWithTag("Stream_ScheduledMessageActions").assertExists()
    }

    private fun setContent(
        onBackPressed: () -> Unit = {},
        componentFactory: ChatComponentFactory = object : ChatComponentFactory {},
    ) {
        composeTestRule.setContent {
            ChatTheme(componentFactory = componentFactory) {
                ScheduledMessagesScreen(
                    viewModelFactory = ScheduledMessageListViewModelFactory(chatClient = client),
                    onBackPressed = onBackPressed,
                )
            }
        }
        composeTestRule.waitForIdle()
    }

    private fun scheduled(
        id: String = "scheduled-1",
        text: String = "Heads up",
        status: ScheduledMessageStatus = ScheduledMessageStatus.Scheduled,
        failure: ScheduledMessageFailure? = null,
    ): ScheduledMessage {
        val now = System.currentTimeMillis()
        val scheduledAt = Date(now + TimeUnit.DAYS.toMillis(1))
        return ScheduledMessage(
            id = id,
            cid = channel.cid,
            channel = ChannelInfo(cid = channel.cid, id = channel.id, type = channel.type, name = channel.name),
            message = Message(id = id, cid = channel.cid, text = text),
            scheduledAt = scheduledAt,
            status = status,
            failure = failure,
            createdAt = Date(now),
            updatedAt = Date(now),
        )
    }
}
