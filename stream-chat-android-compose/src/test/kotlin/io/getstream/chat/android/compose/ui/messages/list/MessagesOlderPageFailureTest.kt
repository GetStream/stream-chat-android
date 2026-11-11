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

package io.getstream.chat.android.compose.ui.messages.list

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipe
import androidx.compose.ui.test.swipeDown
import androidx.compose.ui.unit.dp
import androidx.test.ext.junit.runners.AndroidJUnit4
import io.getstream.chat.android.client.test.MockedChatClientTest
import io.getstream.chat.android.compose.ui.theme.ChatTheme
import io.getstream.chat.android.compose.util.extensions.toSet
import io.getstream.chat.android.models.ChannelCapabilities
import io.getstream.chat.android.models.ConnectionState
import io.getstream.chat.android.models.Message
import io.getstream.chat.android.models.User
import io.getstream.chat.android.randomUser
import io.getstream.chat.android.ui.common.state.messages.list.MessageItemState
import io.getstream.chat.android.ui.common.state.messages.list.MessageListState
import io.getstream.chat.android.ui.common.state.messages.list.TypingItemState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.whenever
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@Config(sdk = [33])
internal class MessagesOlderPageFailureTest : MockedChatClientTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private lateinit var messagesLazyListState: MessagesLazyListState
    private lateinit var scope: CoroutineScope
    private var olderPageRequests = 0
    private val currentUser = User(id = "me")
    private var state by mutableStateOf(MessageListState())

    @Before
    fun prepare() {
        whenever(mockClientState.user) doReturn MutableStateFlow(randomUser())
        whenever(mockClientState.connectionState) doReturn MutableStateFlow(ConnectionState.Connected)
    }

    @Test
    fun `a short list requests a failing older page once`() {
        setMessages(messageCount = 3)

        composeTestRule.mainClock.advanceTimeBy(FAILURE_WINDOW_MS)
        composeTestRule.waitForIdle()

        assertEquals(1, olderPageRequests)
    }

    @Test
    fun `a long list scrolled to the top requests a failing older page once`() {
        setMessages(messageCount = LONG_LIST_SIZE)
        scrollTo(LONG_LIST_SIZE - 1)

        composeTestRule.mainClock.advanceTimeBy(FAILURE_WINDOW_MS)
        composeTestRule.waitForIdle()

        assertEquals(1, olderPageRequests)
    }

    @Test
    fun `a long list stopped at the load threshold requests a failing older page once`() {
        setMessages(messageCount = LONG_LIST_SIZE)
        scrollUntilLastVisibleIndexIs(LONG_LIST_SIZE - LOAD_MORE_THRESHOLD)

        composeTestRule.mainClock.advanceTimeBy(FAILURE_WINDOW_MS)
        composeTestRule.waitForIdle()

        assertEquals(1, olderPageRequests)
    }

    @Test
    fun `a new message after a failed older page allows one more request`() {
        setMessages(messageCount = 3)
        composeTestRule.mainClock.advanceTimeBy(FAILURE_WINDOW_MS)

        composeTestRule.runOnIdle {
            state = state.copy(messageItems = listOf(messageItem(index = 3)) + state.messageItems)
        }
        composeTestRule.mainClock.advanceTimeBy(FAILURE_WINDOW_MS)
        composeTestRule.waitForIdle()

        assertEquals(2, olderPageRequests)
    }

    @Test
    fun `a typing indicator after a failed older page does not allow another request`() {
        setMessages(messageCount = 3)
        composeTestRule.mainClock.advanceTimeBy(FAILURE_WINDOW_MS)

        composeTestRule.runOnIdle {
            state = state.copy(messageItems = listOf(TypingItemState(listOf(User(id = "other")))) + state.messageItems)
        }
        composeTestRule.mainClock.advanceTimeBy(FAILURE_WINDOW_MS)
        composeTestRule.waitForIdle()

        assertEquals(1, olderPageRequests)
    }

    @Test
    fun `dragging a short list after a failed older page allows one more request`() {
        setMessages(messageCount = 3)
        composeTestRule.mainClock.advanceTimeBy(FAILURE_WINDOW_MS)

        composeTestRule.onNodeWithTag("Stream_Messages").performTouchInput { swipeDown() }
        composeTestRule.mainClock.advanceTimeBy(FAILURE_WINDOW_MS)
        composeTestRule.waitForIdle()

        assertEquals(2, olderPageRequests)
    }

    @Test
    fun `dragging at the top of a long list after a failed older page allows one more request`() {
        setMessages(messageCount = LONG_LIST_SIZE)
        scrollTo(LONG_LIST_SIZE - 1)
        composeTestRule.mainClock.advanceTimeBy(FAILURE_WINDOW_MS)

        composeTestRule.onNodeWithTag("Stream_Messages").performTouchInput { swipeDown() }
        composeTestRule.mainClock.advanceTimeBy(FAILURE_WINDOW_MS)
        composeTestRule.waitForIdle()

        assertEquals(2, olderPageRequests)
    }

    @Test
    fun `a short drag within the load threshold after a failed older page allows one more request`() {
        setMessages(messageCount = LONG_LIST_SIZE)
        scrollUntilLastVisibleIndexIs(LONG_LIST_SIZE - LOAD_MORE_THRESHOLD)
        composeTestRule.mainClock.advanceTimeBy(FAILURE_WINDOW_MS)

        composeTestRule.onNodeWithTag("Stream_Messages").performTouchInput {
            swipe(start = center, end = center + Offset(0f, SHORT_DRAG_DP.dp.toPx()), durationMillis = 1_000)
        }
        composeTestRule.mainClock.advanceTimeBy(FAILURE_WINDOW_MS)
        composeTestRule.waitForIdle()

        assertTrue(composeTestRule.runOnIdle { messagesLazyListState.lazyListState.canScrollForward })
        assertEquals(2, olderPageRequests)
    }

    @Test
    fun `a scroll the user did not start after a failed older page does not allow another request`() {
        setMessages(messageCount = LONG_LIST_SIZE)
        scrollTo(LONG_LIST_SIZE - 1)
        composeTestRule.mainClock.advanceTimeBy(FAILURE_WINDOW_MS)

        animateScrollTo(LONG_LIST_SIZE / 2)
        animateScrollTo(LONG_LIST_SIZE - 1)
        composeTestRule.mainClock.advanceTimeBy(FAILURE_WINDOW_MS)
        composeTestRule.waitForIdle()

        assertEquals(1, olderPageRequests)
    }

    private fun messageItem(index: Int) = MessageItemState(
        message = Message(id = "m$index", text = "Message $index", user = currentUser),
        isMine = true,
        currentUser = currentUser,
        ownCapabilities = ChannelCapabilities.toSet(),
    )

    private fun setMessages(messageCount: Int) {
        state = MessageListState(messageItems = (0 until messageCount).map(::messageItem))
        composeTestRule.setContent {
            ChatTheme {
                messagesLazyListState = MessagesLazyListState(rememberLazyListState())
                scope = rememberCoroutineScope()
                Box(modifier = Modifier.size(width = 300.dp, height = 600.dp)) {
                    Messages(
                        messagesState = state,
                        messagesLazyListState = messagesLazyListState,
                        onMessagesStartReached = {
                            // Mirrors MessageListController: a request while one is running is ignored, and every
                            // request fails after a short delay, reporting the failure as the loading ends.
                            if (!state.isLoadingOlderMessages) {
                                olderPageRequests++
                                state = state.copy(isLoadingOlderMessages = true, loadingOlderMessagesError = false)
                                scope.launch {
                                    delay(FAILED_REQUEST_DURATION_MS)
                                    state = state.copy(isLoadingOlderMessages = false, loadingOlderMessagesError = true)
                                }
                            }
                        },
                        onLastVisibleMessageChanged = {},
                        onScrolledToBottom = {},
                        onMessagesEndReached = {},
                        onScrollToBottom = {},
                        helperContent = {},
                        loadingMoreContent = { Box(modifier = Modifier.fillMaxWidth().height(ITEM_HEIGHT_DP.dp)) },
                        itemModifier = { _, _ -> Modifier },
                        itemContent = { Box(modifier = Modifier.fillMaxWidth().height(ITEM_HEIGHT_DP.dp)) },
                    )
                }
            }
        }
        composeTestRule.waitForIdle()
    }

    private fun scrollTo(index: Int) {
        composeTestRule.runOnIdle {
            scope.launch { messagesLazyListState.lazyListState.scrollToItem(index) }
        }
        composeTestRule.waitForIdle()
    }

    // Unlike scrollToItem, spans several frames, so isScrollInProgress is seen as true.
    private fun animateScrollTo(index: Int) {
        composeTestRule.runOnIdle {
            scope.launch { messagesLazyListState.lazyListState.animateScrollToItem(index) }
        }
        composeTestRule.waitForIdle()
    }

    private fun scrollUntilLastVisibleIndexIs(lastVisibleIndex: Int) {
        val lazyListState = messagesLazyListState.lazyListState
        (0 until lastVisibleIndex).first { candidate ->
            scrollTo(candidate)
            composeTestRule.runOnIdle {
                lazyListState.layoutInfo.visibleItemsInfo.lastOrNull()?.index == lastVisibleIndex
            }
        }
    }

    private companion object {
        const val LONG_LIST_SIZE = 50
        const val ITEM_HEIGHT_DP = 50
        const val LOAD_MORE_THRESHOLD = 3
        const val FAILED_REQUEST_DURATION_MS = 100L
        const val FAILURE_WINDOW_MS = 2_000L
        const val SHORT_DRAG_DP = 30
    }
}
