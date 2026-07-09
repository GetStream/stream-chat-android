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

package io.getstream.chat.android.compose.viewmodel.messages

import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.models.QueryReactionsResult
import io.getstream.chat.android.randomReaction
import io.getstream.chat.android.randomString
import io.getstream.chat.android.test.TestCoroutineExtension
import io.getstream.chat.android.test.asCall
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.amshove.kluent.`should be equal to`
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.kotlin.anyOrNull
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.doReturnConsecutively
import org.mockito.kotlin.eq
import org.mockito.kotlin.isNotNull
import org.mockito.kotlin.isNull
import org.mockito.kotlin.mock
import org.mockito.kotlin.times
import org.mockito.kotlin.verify

@ExperimentalCoroutinesApi
@ExtendWith(TestCoroutineExtension::class)
internal class ReactionsMenuViewModelTest {

    private val messageId = randomString()

    @Test
    fun `all reactions are loaded on start`() = runTest {
        val fetched = List(3) { randomReaction(messageId = messageId) }
        val chatClient = mock<ChatClient> {
            on { queryReactions(eq(messageId), anyOrNull(), anyOrNull(), anyOrNull(), anyOrNull()) } doReturn
                QueryReactionsResult(reactions = fetched, next = null).asCall()
        }

        val viewModel = ReactionsMenuViewModel(messageId, emptyList(), chatClient)

        val state = viewModel.state.value
        state.reactions `should be equal to` fetched
        state.isLoading `should be equal to` false
        state.selectedReactionType `should be equal to` null
    }

    @Test
    fun `selectReaction loads reactions filtered by type`() = runTest {
        val all = List(3) { randomReaction(messageId = messageId) }
        val likes = List(2) { randomReaction(messageId = messageId, type = "like") }
        val chatClient = mock<ChatClient> {
            on { queryReactions(eq(messageId), isNull(), anyOrNull(), anyOrNull(), anyOrNull()) } doReturn
                QueryReactionsResult(reactions = all, next = null).asCall()
            on { queryReactions(eq(messageId), isNotNull(), anyOrNull(), anyOrNull(), anyOrNull()) } doReturn
                QueryReactionsResult(reactions = likes, next = null).asCall()
        }
        val viewModel = ReactionsMenuViewModel(messageId, emptyList(), chatClient)

        viewModel.selectReaction("like")

        val state = viewModel.state.value
        state.selectedReactionType `should be equal to` "like"
        state.reactions `should be equal to` likes
        state.isLoading `should be equal to` false
    }

    @Test
    fun `selecting the same type again clears the filter using the cached page`() = runTest {
        val all = List(3) { randomReaction(messageId = messageId) }
        val likes = List(2) { randomReaction(messageId = messageId, type = "like") }
        val chatClient = mock<ChatClient> {
            on { queryReactions(eq(messageId), isNull(), anyOrNull(), anyOrNull(), anyOrNull()) } doReturn
                QueryReactionsResult(reactions = all, next = null).asCall()
            on { queryReactions(eq(messageId), isNotNull(), anyOrNull(), anyOrNull(), anyOrNull()) } doReturn
                QueryReactionsResult(reactions = likes, next = null).asCall()
        }
        val viewModel = ReactionsMenuViewModel(messageId, emptyList(), chatClient)

        viewModel.selectReaction("like")
        viewModel.selectReaction("like")

        val state = viewModel.state.value
        state.selectedReactionType `should be equal to` null
        state.reactions `should be equal to` all
        verify(chatClient, times(1))
            .queryReactions(eq(messageId), isNull(), anyOrNull(), anyOrNull(), anyOrNull())
    }

    @Test
    fun `loadMore appends the next page`() = runTest {
        val firstPage = List(3) { randomReaction(messageId = messageId) }
        val secondPage = List(2) { randomReaction(messageId = messageId) }
        val chatClient = mock<ChatClient> {
            on {
                queryReactions(eq(messageId), anyOrNull(), anyOrNull(), anyOrNull(), anyOrNull())
            } doReturnConsecutively listOf(
                QueryReactionsResult(reactions = firstPage, next = randomString()).asCall(),
                QueryReactionsResult(reactions = secondPage, next = null).asCall(),
            )
        }
        val viewModel = ReactionsMenuViewModel(messageId, emptyList(), chatClient)

        viewModel.loadMore()

        val state = viewModel.state.value
        state.reactions `should be equal to` firstPage + secondPage
        state.isLoadingMore `should be equal to` false
    }

    @Test
    fun `loadMore does nothing when all reactions are loaded`() = runTest {
        val fetched = List(3) { randomReaction(messageId = messageId) }
        val chatClient = mock<ChatClient> {
            on { queryReactions(eq(messageId), anyOrNull(), anyOrNull(), anyOrNull(), anyOrNull()) } doReturn
                QueryReactionsResult(reactions = fetched, next = null).asCall()
        }
        val viewModel = ReactionsMenuViewModel(messageId, emptyList(), chatClient)

        viewModel.loadMore()

        viewModel.state.value.reactions `should be equal to` fetched
        verify(chatClient, times(1))
            .queryReactions(eq(messageId), anyOrNull(), anyOrNull(), anyOrNull(), anyOrNull())
    }
}
