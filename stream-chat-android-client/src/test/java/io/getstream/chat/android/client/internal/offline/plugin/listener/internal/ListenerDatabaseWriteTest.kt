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

package io.getstream.chat.android.client.internal.offline.plugin.listener.internal

import io.getstream.chat.android.client.api.models.QueryChannelRequest
import io.getstream.chat.android.client.api.models.QueryThreadsRequest
import io.getstream.chat.android.client.internal.offline.repository.database.internal.WriteRecorder
import io.getstream.chat.android.client.persistance.repository.RepositoryFacade
import io.getstream.chat.android.client.query.CreateChannelParams
import io.getstream.chat.android.client.setup.state.ClientState
import io.getstream.chat.android.models.Filters
import io.getstream.chat.android.models.Member
import io.getstream.chat.android.models.Message
import io.getstream.chat.android.models.QueryDraftsResult
import io.getstream.chat.android.models.SyncStatus
import io.getstream.chat.android.models.User
import io.getstream.chat.android.models.querysort.QuerySortByField
import io.getstream.chat.android.randomChannel
import io.getstream.chat.android.randomDraftMessage
import io.getstream.chat.android.randomMember
import io.getstream.chat.android.randomMessage
import io.getstream.chat.android.randomQueryThreadsResult
import io.getstream.chat.android.randomReaction
import io.getstream.chat.android.randomString
import io.getstream.chat.android.randomUser
import io.getstream.result.Result
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.amshove.kluent.shouldBeEqualTo
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import org.mockito.Mockito
import org.mockito.invocation.InvocationOnMock
import org.mockito.kotlin.any
import org.mockito.kotlin.doAnswer
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.stubbing.Answer

internal class ListenerDatabaseWriteTest {

    @ParameterizedTest(name = "{0}")
    @MethodSource("hooks")
    fun `a hook reads and stores in one write`(name: String, hook: suspend Fixture.() -> Unit) = runTest {
        val fixture = Fixture()

        fixture.hook()

        fixture.recorder.writes shouldBeEqualTo 1
        fixture.callsOutsideWrite shouldBeEqualTo emptyList()
    }

    /** One mocked facade stands for every repository, as in the plugin, and records the calls made outside a write. */
    internal class Fixture {
        val recorder = WriteRecorder()
        val callsOutsideWrite = mutableListOf<String>()
        val store = recorder.store
        val clientState: ClientState = mock { on { it.user } doReturn MutableStateFlow(null) }
        val type = randomString()
        val id = randomString()
        val cid = "$type:$id"
        val user = randomUser()
        val message = randomMessage(cid = cid)
        val reaction = randomReaction(messageId = message.id, user = user)
        val draft = randomDraftMessage()

        /** The message the repositories return. Null by default, as the hooks that send one expect none stored. */
        var storedMessage: Message? = null

        val repos: RepositoryFacade = mock(
            defaultAnswer = { invocation ->
                record(invocation)
                Mockito.RETURNS_DEFAULTS.answer(invocation)
            },
        ) {
            on { it.store } doReturn store
            onBlocking { it.selectMessage(any()) } doAnswer recorded { storedMessage }
            onBlocking { it.selectUsers(any()) } doAnswer recorded { emptyList<User>() }
            onBlocking { it.selectUserReactionToMessage(any(), any(), any()) } doAnswer recorded { reaction }
        }

        init {
            // Stubbing calls the mock too.
            callsOutsideWrite.clear()
        }

        private fun record(invocation: InvocationOnMock) {
            if (!recorder.isWriting) callsOutsideWrite += invocation.method.name
        }

        private fun <T> recorded(value: () -> T) = Answer { invocation ->
            record(invocation)
            value()
        }
    }

    companion object {

        private fun hook(name: String, hook: suspend Fixture.() -> Unit) = Arguments.of(name, hook)

        @JvmStatic
        @Suppress("LongMethod")
        fun hooks() = listOf(
            hook("send attachments request") {
                SendAttachmentsListenerDatabase(store, repos, repos).onAttachmentSendRequest(type, id, message)
            },
            hook("send giphy result") {
                SendGiphyListenerDatabase(store, repos, repos).onGiphySendResult(cid, Result.Success(message))
            },
            hook("send message result") {
                SendMessageListenerDatabase(store, repos, repos, clientState)
                    .onMessageSendResult(Result.Success(message), type, id, message)
            },
            hook("send reaction request") {
                storedMessage = message
                SendReactionListenerDatabase(store, clientState, repos, repos, repos, emptySet())
                    .onSendReactionRequest(cid, reaction, enforceUnique = true, skipPush = false, currentUser = user)
            },
            hook("send reaction result") {
                SendReactionListenerDatabase(store, clientState, repos, repos, repos, emptySet())
                    .onSendReactionResult(cid, reaction, enforceUnique = false, user, Result.Success(reaction))
            },
            hook("delete message for me request") {
                storedMessage = message
                DeleteMessageForMeListenerDatabase(store, clientState, repos).onDeleteMessageForMeRequest(message.id)
            },
            hook("delete message for me result") {
                DeleteMessageForMeListenerDatabase(store, clientState, repos)
                    .onDeleteMessageForMeResult(message.id, Result.Success(message))
            },
            hook("delete message precondition") {
                storedMessage = message.copy(syncStatus = SyncStatus.FAILED_PERMANENTLY)
                DeleteMessageListenerDatabase(store, clientState, repos, repos).onMessageDeletePrecondition(message.id)
            },
            hook("delete message request") {
                storedMessage = message
                DeleteMessageListenerDatabase(store, clientState, repos, repos)
                    .onMessageDeleteRequest(message.id, hard = false)
            },
            hook("delete message result") {
                DeleteMessageListenerDatabase(store, clientState, repos, repos)
                    .onMessageDeleteResult(message.id, Result.Success(message))
            },
            hook("delete reaction request") {
                storedMessage = message
                DeleteReactionListenerDatabase(store, clientState, repos, repos)
                    .onDeleteReactionRequest(cid, message.id, reaction.type, user)
            },
            hook("delete reaction result") {
                DeleteReactionListenerDatabase(store, clientState, repos, repos)
                    .onDeleteReactionResult(cid, message.id, reaction.type, user, Result.Success(message))
            },
            hook("edit message request") {
                EditMessageListenerDatabase(store, repos, repos, clientState).onMessageEditRequest(message)
            },
            hook("edit message result") {
                EditMessageListenerDatabase(store, repos, repos, clientState)
                    .onMessageEditResult(message, Result.Success(message))
            },
            hook("shuffle giphy result") {
                ShuffleGiphyListenerDatabase(store, repos, repos).onShuffleGiphyResult(cid, Result.Success(message))
            },
            hook("create draft request") {
                DraftMessageListenerDatabase(store, repos).onCreateDraftMessageRequest(type, id, draft)
            },
            hook("create draft result") {
                DraftMessageListenerDatabase(store, repos)
                    .onCreateDraftMessageResult(Result.Success(draft), type, id, draft)
            },
            hook("delete draft request") {
                DraftMessageListenerDatabase(store, repos).onDeleteDraftMessagesRequest(type, id, draft)
            },
            hook("delete draft result") {
                DraftMessageListenerDatabase(store, repos)
                    .onDeleteDraftMessagesResult(Result.Success(Unit), type, id, draft)
            },
            hook("query drafts result") {
                DraftMessageListenerDatabase(store, repos).onQueryDraftMessagesResult(
                    Result.Success(QueryDraftsResult(listOf(draft, randomDraftMessage()), next = null)),
                    Filters.neutral(),
                    limit = 2,
                    next = null,
                    sort = QuerySortByField.ascByName("created_at"),
                )
            },
            hook("fetch current user result") {
                FetchCurrentUserListenerDatabase(store, repos).onFetchCurrentUserResult(Result.Success(user))
            },
            hook("hide channel result") {
                HideChannelListenerDatabase(store, repos, repos)
                    .onHideChannelResult(Result.Success(Unit), type, id, clearHistory = true)
            },
            hook("query members result") {
                val members = listOf(randomMember())
                QueryMembersListenerDatabase(store, repos, repos).onQueryMembersResult(
                    Result.Success(members),
                    type,
                    id,
                    offset = 0,
                    limit = 1,
                    filter = Filters.neutral(),
                    sort = QuerySortByField<Member>(),
                    members = emptyList(),
                )
            },
            hook("query threads result") {
                QueryThreadsListenerDatabase(store, repos)
                    .onQueryThreadsResult(Result.Success(randomQueryThreadsResult()), QueryThreadsRequest())
            },
            hook("get replies result") {
                ThreadQueryListenerDatabase(store, repos, repos)
                    .onGetRepliesResult(Result.Success(listOf(message)), message.id, limit = 1)
            },
            hook("create channel request") {
                CreateChannelListenerDatabase(clientState, repos, repos)
                    .onCreateChannelRequest(type, id, CreateChannelParams(emptyList(), emptyMap()), user)
            },
            hook("create channel result") {
                CreateChannelListenerDatabase(clientState, repos, repos)
                    .onCreateChannelResult(type, id, emptyList(), Result.Success(randomChannel()))
            },
            hook("get message result") {
                GetMessageListenerDatabase(repos).onGetMessageResult(message.id, Result.Success(message))
            },
            hook("query channel result") {
                QueryChannelListenerDatabase(repos)
                    .onQueryChannelResult(Result.Success(randomChannel()), type, id, QueryChannelRequest())
            },
        )
    }
}
