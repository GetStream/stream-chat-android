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

package io.getstream.chat.android.state.plugin.listener.internal

import io.getstream.chat.android.client.api.models.QueryThreadsRequest
import io.getstream.chat.android.models.QueryThreadsResult
import io.getstream.chat.android.models.Thread
import io.getstream.chat.android.state.plugin.logic.internal.LogicRegistry
import io.getstream.chat.android.state.plugin.logic.querythreads.internal.QueryThreadsLogic
import io.getstream.result.Error
import io.getstream.result.Result
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

internal class QueryThreadsListenerStateTest {

    private val queryThreadsLogic = mock<QueryThreadsLogic>()
    private val logicRegistry = mock<LogicRegistry>()
    private val queryThreadsListenerState = QueryThreadsListenerState(logicRegistry)

    @Test
    fun `onQueryThreadsPrecondition should delegate to LogicRegistry threads logic`() = runTest {
        // Given
        val request = QueryThreadsRequest()
        val expectedResult = Result.Success(Unit)
        whenever(logicRegistry.threads(request)) doReturn queryThreadsLogic
        whenever(queryThreadsLogic.onQueryThreadsPrecondition(request)) doReturn expectedResult

        // When
        val result = queryThreadsListenerState.onQueryThreadsPrecondition(request)

        // Then
        verify(queryThreadsLogic).onQueryThreadsPrecondition(request)
        Assertions.assertEquals(expectedResult, result)
    }

    @Test
    fun `onQueryThreadsPrecondition should return failure when logic returns failure`() = runTest {
        // Given
        val request = QueryThreadsRequest()
        val expectedResult = Result.Failure(Error.GenericError("Test error"))
        whenever(logicRegistry.threads(request)) doReturn queryThreadsLogic
        whenever(queryThreadsLogic.onQueryThreadsPrecondition(request)) doReturn expectedResult

        // When
        val result = queryThreadsListenerState.onQueryThreadsPrecondition(request)

        // Then
        verify(queryThreadsLogic).onQueryThreadsPrecondition(request)
        Assertions.assertEquals(expectedResult, result)
    }

    @Test
    fun `onQueryThreadsRequest should delegate to LogicRegistry threads logic`() = runTest {
        // Given
        val request = QueryThreadsRequest()
        whenever(logicRegistry.threads(request)) doReturn queryThreadsLogic

        // When
        queryThreadsListenerState.onQueryThreadsRequest(request)

        // Then
        verify(queryThreadsLogic).onQueryThreadsRequest(request)
    }

    @Test
    fun `onQueryThreadsResult should delegate to LogicRegistry threads logic with success result`() = runTest {
        // Given
        val request = QueryThreadsRequest()
        val threads = listOf<Thread>()
        val queryResult = QueryThreadsResult(threads = threads, prev = null, next = null)
        val result = Result.Success(queryResult)
        whenever(logicRegistry.threads(request)) doReturn queryThreadsLogic

        // When
        queryThreadsListenerState.onQueryThreadsResult(result, request)

        // Then
        verify(queryThreadsLogic).onQueryThreadsResult(result, request)
    }
}
