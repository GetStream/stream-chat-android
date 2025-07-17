/*
 * Copyright (c) 2014-2025 Stream.io Inc. All rights reserved.
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

package io.getstream.chat.android.client

import io.getstream.chat.android.client.chatclient.BaseChatClientTest
import io.getstream.chat.android.client.plugin.Plugin
import io.getstream.chat.android.client.utils.RetroError
import io.getstream.chat.android.client.utils.RetroSuccess
import io.getstream.chat.android.client.utils.verifyNetworkError
import io.getstream.chat.android.client.utils.verifySuccess
import io.getstream.chat.android.models.QueryThreadsResult
import io.getstream.chat.android.models.Thread
import io.getstream.chat.android.positiveRandomInt
import io.getstream.chat.android.randomQueryThreadsResult
import io.getstream.chat.android.randomString
import io.getstream.chat.android.randomThread
import io.getstream.result.Result
import io.getstream.result.call.Call
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import org.mockito.Mockito.spy
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

/**
 * Tests for the Threads functionality of the [ChatClient].
 */
internal class ChatClientThreadsApiTest : BaseChatClientTest() {

    @Test
    fun queryThreadsSuccess() = runTest {
        // given
        val query = Mother.randomQueryThreadsRequest()
        val result = randomQueryThreadsResult()
        val plugin = mock<Plugin>()
        val sut = spy(
            Fixture()
                .givenPlugin(plugin)
                .givenQueryThreadsResult(RetroSuccess(result).toRetrofitCall())
                .get(),
        )
        // when
        val actual = sut.queryThreads(query).await()
        // then
        verifySuccess(actual, result.threads)
        verify(sut).queryThreadsResult(query)
        val inOrder = Mockito.inOrder(plugin)
        inOrder.verify(plugin).onQueryThreadsPrecondition(query)
        inOrder.verify(plugin).onQueryThreadsRequest(query)
        inOrder.verify(plugin).onQueryThreadsResult(Result.Success(result), query)
    }

    @Test
    fun queryThreadsError() = runTest {
        // given
        val query = Mother.randomQueryThreadsRequest()
        val errorCode = positiveRandomInt()
        val sut = spy(
            Fixture()
                .givenQueryThreadsResult(RetroError<QueryThreadsResult>(errorCode).toRetrofitCall())
                .get(),
        )
        // when
        val actual = sut.queryThreads(query).await()
        // then
        verifyNetworkError(actual, errorCode)
        verify(sut).queryThreadsResult(query)
    }

    @Test
    fun queryThreadsResultSuccess() = runTest {
        // given
        val query = Mother.randomQueryThreadsRequest()
        val result = randomQueryThreadsResult()
        val plugin = mock<Plugin>()
        val sut = Fixture()
            .givenPlugin(plugin)
            .givenQueryThreadsResult(RetroSuccess(result).toRetrofitCall())
            .get()
        // when
        val actual = sut.queryThreadsResult(query).await()
        // then
        verifySuccess(actual, result)
        val inOrder = Mockito.inOrder(plugin)
        inOrder.verify(plugin).onQueryThreadsPrecondition(query)
        inOrder.verify(plugin).onQueryThreadsRequest(query)
        inOrder.verify(plugin).onQueryThreadsResult(Result.Success(result), query)
    }

    @Test
    fun queryThreadsResultError() = runTest {
        // given
        val query = Mother.randomQueryThreadsRequest()
        val plugin = mock<Plugin>()
        val errorCode = positiveRandomInt()
        val sut = Fixture()
            .givenPlugin(plugin)
            .givenQueryThreadsResult(RetroError<QueryThreadsResult>(errorCode).toRetrofitCall())
            .get()
        // when
        val actual = sut.queryThreadsResult(query).await()
        // then
        verifyNetworkError(actual, errorCode)
        val inOrder = Mockito.inOrder(plugin)
        inOrder.verify(plugin).onQueryThreadsPrecondition(query)
        inOrder.verify(plugin).onQueryThreadsRequest(query)
        inOrder.verify(plugin).onQueryThreadsResult(actual, query)
    }

    @Test
    fun getThreadSuccess() = runTest {
        // given
        val messageId = randomString()
        val thread = randomThread(parentMessageId = messageId)
        val sut = Fixture()
            .givenGetThreadResult(RetroSuccess(thread).toRetrofitCall())
            .get()
        // when
        val actual = sut.getThread(messageId).await()
        // then
        verifySuccess(actual, thread)
    }

    @Test
    fun getThreadError() = runTest {
        // given
        val messageId = randomString()
        val errorCode = positiveRandomInt()
        val sut = Fixture()
            .givenGetThreadResult(RetroError<Thread>(errorCode).toRetrofitCall())
            .get()
        // when
        val actual = sut.getThread(messageId).await()
        // then
        verifyNetworkError(actual, errorCode)
    }

    @Test
    fun partialUpdateThreadSuccess() = runTest {
        // given
        val messageId = randomString()
        val set = emptyMap<String, Any>()
        val unset = emptyList<String>()
        val thread = randomThread(parentMessageId = messageId)
        val sut = Fixture()
            .givenUpdateThreadResult(RetroSuccess(thread).toRetrofitCall())
            .get()
        // when
        val actual = sut.partialUpdateThread(messageId, set, unset).await()
        // then
        verifySuccess(actual, thread)
    }

    @Test
    fun partialUpdateThreadError() = runTest {
        // given
        val messageId = randomString()
        val set = emptyMap<String, Any>()
        val unset = emptyList<String>()
        val errorCode = positiveRandomInt()
        val sut = Fixture()
            .givenUpdateThreadResult(RetroError<Thread>(errorCode).toRetrofitCall())
            .get()
        // when
        val actual = sut.partialUpdateThread(messageId, set, unset).await()
        // then
        verifyNetworkError(actual, errorCode)
    }

    internal inner class Fixture {

        fun givenQueryThreadsResult(call: Call<QueryThreadsResult>) = apply {
            whenever(api.queryThreads(any())).thenReturn(call)
        }

        fun givenGetThreadResult(call: Call<Thread>) = apply {
            whenever(api.getThread(any(), any())).thenReturn(call)
        }

        fun givenUpdateThreadResult(call: Call<Thread>) = apply {
            whenever(api.partialUpdateThread(any(), any(), any())).thenReturn(call)
        }

        fun givenPlugin(plugin: Plugin) = apply {
            plugins.add(plugin)
        }

        fun get(): ChatClient = chatClient
    }
}
