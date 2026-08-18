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

package io.getstream.chat.android.client.plugin.listeners

import io.getstream.chat.android.client.plugin.Plugin
import io.getstream.chat.android.core.internal.InternalStreamChatApi
import io.getstream.chat.android.models.DraftMessage
import io.getstream.chat.android.models.DraftsSort
import io.getstream.chat.android.models.FilterObject
import io.getstream.chat.android.models.QueryDraftsResult
import io.getstream.chat.android.models.User
import io.getstream.chat.android.models.querysort.QuerySorter
import io.getstream.chat.android.randomDraftMessage
import io.getstream.chat.android.randomString
import io.getstream.result.Result
import kotlinx.coroutines.test.runTest
import org.amshove.kluent.shouldBeEmpty
import org.junit.jupiter.api.Test
import kotlin.reflect.KClass

internal class DraftMessageListenerTest {

    @Test
    fun `a listener that does not override the request hooks should still be invokable`() = runTest {
        val listener = RecordingDraftMessageListener()

        listener.onCreateDraftMessageRequest(randomString(), randomString(), randomDraftMessage())
        listener.onDeleteDraftMessagesRequest(randomString(), randomString(), randomDraftMessage())

        listener.invocations.shouldBeEmpty()
    }

    @Test
    fun `a plugin that does not override the request hooks should still be invokable`() = runTest {
        val plugin = BarePlugin()

        plugin.onCreateDraftMessageRequest(randomString(), randomString(), randomDraftMessage())
        plugin.onDeleteDraftMessagesRequest(randomString(), randomString(), randomDraftMessage())
    }

    private class RecordingDraftMessageListener : DraftMessageListener {

        val invocations: MutableList<String> = mutableListOf()

        override suspend fun onCreateDraftMessageResult(
            result: Result<DraftMessage>,
            channelType: String,
            channelId: String,
            message: DraftMessage,
        ) {
            invocations += "onCreateDraftMessageResult"
        }

        override suspend fun onDeleteDraftMessagesResult(
            result: Result<Unit>,
            channelType: String,
            channelId: String,
            message: DraftMessage,
        ) {
            invocations += "onDeleteDraftMessagesResult"
        }

        override suspend fun onQueryDraftMessagesResult(
            result: Result<List<DraftMessage>>,
            offset: Int?,
            limit: Int?,
        ) {
            invocations += "onQueryDraftMessagesResult"
        }

        override suspend fun onQueryDraftMessagesResult(
            result: Result<QueryDraftsResult>,
            filter: FilterObject,
            limit: Int,
            next: String?,
            sort: QuerySorter<DraftsSort>,
        ) {
            invocations += "onQueryDraftMessagesResult"
        }
    }

    private class BarePlugin : Plugin {
        override fun onUserSet(user: User) { /* No-Op */ }
        override fun onUserDisconnected() { /* No-Op */ }

        @InternalStreamChatApi
        override fun <T : Any> resolveDependency(klass: KClass<T>): T? = null
    }
}
