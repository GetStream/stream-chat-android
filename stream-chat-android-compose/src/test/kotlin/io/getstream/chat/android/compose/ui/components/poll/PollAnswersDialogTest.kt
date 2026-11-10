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

package io.getstream.chat.android.compose.ui.components.poll

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasScrollAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performScrollToNode
import androidx.test.ext.junit.runners.AndroidJUnit4
import io.getstream.chat.android.client.test.MockedChatClientTest
import io.getstream.chat.android.compose.ui.theme.ChatTheme
import io.getstream.chat.android.compose.viewmodel.messages.MessageListViewModel
import io.getstream.chat.android.models.QueryPollAnswersResult
import io.getstream.chat.android.randomMessage
import io.getstream.chat.android.randomPoll
import io.getstream.chat.android.randomPollAnswer
import io.getstream.chat.android.randomString
import io.getstream.chat.android.randomUser
import io.getstream.chat.android.test.asCall
import io.getstream.chat.android.ui.common.state.messages.poll.PollSelectionType
import io.getstream.chat.android.ui.common.state.messages.poll.SelectedPoll
import io.getstream.result.Error
import io.getstream.result.call.Call
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.any
import org.mockito.kotlin.anyOrNull
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.doReturnConsecutively
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import org.robolectric.annotation.Config
import org.robolectric.shadows.ShadowToast
import java.util.Date

@RunWith(AndroidJUnit4::class)
@Config(sdk = [33])
internal class PollAnswersDialogTest : MockedChatClientTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val poll = randomPoll(answers = emptyList())

    // Load more only triggers once the list is longer than its threshold of 3 items
    private val firstPage = List(4) { randomPollAnswer(pollId = poll.id, text = "First page answer $it") }

    @Test
    fun `shows the next page of answers`() {
        val older = randomPollAnswer(pollId = poll.id, text = "Second page answer", createdAt = Date(0))
        stubQueryPollAnswers(
            QueryPollAnswersResult(answers = firstPage, next = randomString()).asCall(),
            QueryPollAnswersResult(answers = listOf(older), next = null).asCall(),
        )

        setContent()

        composeTestRule.onNode(hasScrollAction()).performScrollToNode(hasText(older.text))
        composeTestRule.onNodeWithText(older.text).assertIsDisplayed()
    }

    @Test
    fun `a failed page shows an error toast`() {
        stubQueryPollAnswers(
            QueryPollAnswersResult(answers = firstPage, next = randomString()).asCall(),
            Error.GenericError(randomString()).asCall(),
        )

        setContent()

        composeTestRule.onNodeWithText(firstPage.first().text).assertExists()
        assertEquals(
            "An error occurred while loading the poll comments. Please try again later.",
            ShadowToast.getTextOfLatestToast(),
        )
    }

    private fun stubQueryPollAnswers(vararg calls: Call<QueryPollAnswersResult>) {
        whenever(mockChatClient.queryPollAnswers(any(), anyOrNull(), anyOrNull(), anyOrNull(), anyOrNull()))
            .doReturnConsecutively(calls.toList())
    }

    private fun setContent() {
        val listViewModel = mock<MessageListViewModel> {
            on { user } doReturn MutableStateFlow(randomUser())
        }
        composeTestRule.setContent {
            ChatTheme {
                PollAnswersDialog(
                    selectedPoll = SelectedPoll(
                        poll = poll,
                        message = randomMessage(),
                        pollSelectionType = PollSelectionType.ViewAnswers,
                    ),
                    listViewModel = listViewModel,
                    onDismissRequest = {},
                    onBackPressed = {},
                )
            }
        }
        composeTestRule.waitForIdle()
    }
}
