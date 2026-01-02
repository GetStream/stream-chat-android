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

package io.getstream.chat.android.ui.common.feature.channel.attachments

import app.cash.turbine.test
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.extensions.cidToTypeAndId
import io.getstream.chat.android.models.Attachment
import io.getstream.chat.android.models.Filters
import io.getstream.chat.android.models.SearchMessagesResult
import io.getstream.chat.android.randomAttachment
import io.getstream.chat.android.randomCID
import io.getstream.chat.android.randomMessage
import io.getstream.chat.android.randomString
import io.getstream.chat.android.test.asCall
import io.getstream.chat.android.ui.common.state.channel.attachments.ChannelAttachmentsViewState
import io.getstream.result.Error
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertInstanceOf
import org.mockito.kotlin.doAnswer
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

internal class ChannelAttachmentsViewControllerTest {

    @Test
    fun `when initialized, should set state to Loading`() = runTest {
        val sut = Fixture().get(backgroundScope)

        sut.state.value.let { state ->
            assertTrue(state is ChannelAttachmentsViewState.Loading)
        }
    }

    @Test
    fun `when initial load succeeds, should update state with content`() = runTest {
        val attachmentType = randomString()
        val attachment1 = randomAttachment(type = attachmentType)
        val attachment2 = randomAttachment(type = attachmentType)
        val message1 = randomMessage(
            cid = CID,
            attachments = listOf(attachment1, randomAttachment()),
        )
        val message2 = randomMessage(
            cid = CID,
            attachments = listOf(attachment2, randomAttachment()),
        )
        val searchMessagesResult = SearchMessagesResult(
            messages = listOf(message1, message2),
            next = randomString(),
        )
        val sut = Fixture()
            .givenSearchMessagesResult(result = searchMessagesResult)
            .get(backgroundScope, localFilter = { it.type == attachmentType })

        sut.state.test {
            skipItems(1) // Skip initial state
            val viewState = awaitItem()

            assertTrue(viewState is ChannelAttachmentsViewState.Content)
            viewState as ChannelAttachmentsViewState.Content
            val expectedItems = listOf(
                ChannelAttachmentsViewState.Content.Item(message1, attachment1),
                ChannelAttachmentsViewState.Content.Item(message2, attachment2),
            )
            assertEquals(expectedItems, viewState.items)
            assertTrue(viewState.canLoadMore)
            assertFalse(viewState.isLoadingMore)
        }
    }

    @Test
    fun `when initial load fails, should update state with error`() = runTest {
        val error = Error.GenericError("error")
        val sut = Fixture()
            .givenSearchMessagesResult(error = error)
            .get(backgroundScope)

        sut.state.test {
            skipItems(1) // Skip initial state
            val viewState = awaitItem()

            assertTrue(viewState is ChannelAttachmentsViewState.Error)
            viewState as ChannelAttachmentsViewState.Error
            assertEquals("error", viewState.message)
        }
    }

    @Test
    fun `when load more succeeds, should append items to state`() = runTest {
        val attachment1 = randomAttachment()
        val message1 = randomMessage(cid = CID, attachments = listOf(attachment1))
        val nextPage = randomString()
        val firstSearchMessagesResult = SearchMessagesResult(
            messages = listOf(message1),
            next = nextPage,
        )
        val attachment2 = randomAttachment()
        val message2 = randomMessage(cid = CID, attachments = listOf(attachment2))
        val secondSearchMessagesResult = SearchMessagesResult(
            messages = listOf(message2),
            next = null,
        )
        val sut = Fixture()
            .givenSearchMessagesResult(next = null, result = firstSearchMessagesResult)
            .givenSearchMessagesResult(next = nextPage, result = secondSearchMessagesResult)
            .get(backgroundScope)

        sut.state.test {
            skipItems(1) // Skip initial state

            val expectedFirstPageItems = listOf(
                ChannelAttachmentsViewState.Content.Item(message1, attachment1),
            )
            val firstPageViewState = awaitItem() as ChannelAttachmentsViewState.Content
            assertEquals(expectedFirstPageItems, firstPageViewState.items)

            sut.onViewAction(ChannelAttachmentsViewAction.LoadMoreRequested)

            val loadingMoreViewState = awaitItem() as ChannelAttachmentsViewState.Content
            assertEquals(expectedFirstPageItems, loadingMoreViewState.items)
            assertTrue(loadingMoreViewState.canLoadMore)
            assertTrue(loadingMoreViewState.isLoadingMore)

            val expectedAccumulatedItems = expectedFirstPageItems + listOf(
                ChannelAttachmentsViewState.Content.Item(message2, attachment2),
            )
            val finalViewState = awaitItem() as ChannelAttachmentsViewState.Content
            assertEquals(expectedAccumulatedItems, finalViewState.items)
            assertFalse(finalViewState.canLoadMore)
            assertFalse(finalViewState.isLoadingMore)
        }
    }

    @Test
    fun `when load more fails, should emit error event and retain state`() = runTest {
        val attachment1 = randomAttachment()
        val message1 = randomMessage(cid = CID, attachments = listOf(attachment1))
        val nextPage = randomString()
        val firstSearchMessagesResult = SearchMessagesResult(
            messages = listOf(message1),
            next = nextPage,
        )
        val searchingError = Error.GenericError("error")
        val sut = Fixture()
            .givenSearchMessagesResult(result = firstSearchMessagesResult)
            .givenSearchMessagesResult(next = nextPage, error = searchingError)
            .get(backgroundScope)

        sut.state.test {
            skipItems(1) // Skip initial state

            val firstPageViewState = awaitItem() as ChannelAttachmentsViewState.Content
            assertFalse(firstPageViewState.isLoadingMore)

            sut.onViewAction(ChannelAttachmentsViewAction.LoadMoreRequested)

            val loadingMoreViewState = awaitItem() as ChannelAttachmentsViewState.Content
            assertTrue(loadingMoreViewState.isLoadingMore)

            sut.events.test {
                val event = awaitItem()
                assertInstanceOf<ChannelAttachmentsViewEvent.LoadMoreError>(event)
                assertEquals(searchingError, event.error)
            }

            val expectedFinalItems = listOf(
                ChannelAttachmentsViewState.Content.Item(message1, attachment1),
            )
            val finalViewState = awaitItem() as ChannelAttachmentsViewState.Content
            assertEquals(expectedFinalItems, finalViewState.items)
            assertTrue(finalViewState.canLoadMore)
            assertFalse(finalViewState.isLoadingMore)
        }
    }

    @Test
    fun `when no more items to load, should not emit events`() = runTest {
        val sut = Fixture()
            .givenSearchMessagesResult(result = SearchMessagesResult())
            .get(backgroundScope)

        sut.state.test {
            skipItems(1) // Skip initial state

            val viewState = awaitItem() as ChannelAttachmentsViewState.Content
            assertFalse(viewState.isLoadingMore)

            sut.onViewAction(ChannelAttachmentsViewAction.LoadMoreRequested)

            expectNoEvents()
        }
    }

    @Test
    fun `when already loading more, should not emit duplicate events`() = runTest {
        val sut = Fixture()
            .givenSearchMessagesResult(result = SearchMessagesResult(next = randomString()))
            .get(backgroundScope)

        sut.state.test {
            skipItems(1) // Skip initial state

            val viewState = awaitItem() as ChannelAttachmentsViewState.Content
            assertFalse(viewState.isLoadingMore)

            sut.onViewAction(ChannelAttachmentsViewAction.LoadMoreRequested)

            val loadingMoreViewState = awaitItem() as ChannelAttachmentsViewState.Content
            assertTrue(loadingMoreViewState.isLoadingMore)

            sut.onViewAction(ChannelAttachmentsViewAction.LoadMoreRequested)

            expectNoEvents()
        }
    }

    @Test
    fun `when in initial state, should not allow load more`() = runTest {
        val sut = Fixture().get(backgroundScope)

        sut.state.test {
            skipItems(1) // Skip initial state

            sut.onViewAction(ChannelAttachmentsViewAction.LoadMoreRequested)

            expectNoEvents()
        }
    }
}

private val CID = randomCID()
private val ATTACHMENT_TYPE = randomString()

private class Fixture {

    private val chatClient: ChatClient = mock()

    fun givenSearchMessagesResult(
        next: String? = null,
        result: SearchMessagesResult? = null,
        error: Error? = null,
    ) = apply {
        val (channelType, channelId) = CID.cidToTypeAndId()
        whenever(
            chatClient.searchMessages(
                channelFilter = Filters.eq("cid", "$channelType:$channelId"),
                messageFilter = Filters.`in`("attachments.type", listOf(ATTACHMENT_TYPE)),
                offset = null,
                limit = 30,
                next = next,
                sort = null,
            ),
        ) doAnswer { result?.asCall() ?: error?.asCall() }
    }

    fun get(scope: CoroutineScope, localFilter: (Attachment) -> Boolean = { true }) = ChannelAttachmentsViewController(
        cid = CID,
        attachmentTypes = listOf(ATTACHMENT_TYPE),
        localFilter = localFilter,
        chatClient = chatClient,
        scope = scope,
    )
}
