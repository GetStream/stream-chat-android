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

package io.getstream.chat.android.client.plugins.requests

import org.amshove.kluent.shouldBeEqualTo
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import java.text.DateFormat

internal class ApiRequestsDumperTest {

    private lateinit var apiRequestsDumper: ApiRequestsDumper

    private val mockTimestamp = "15:21:44.005"

    @Before
    fun setUp() {
        val dateFormat = mock<DateFormat>()
        whenever(dateFormat.format(any())).thenReturn(mockTimestamp)
        apiRequestsDumper = ApiRequestsDumper(dateFormat)
    }

    @Test
    fun `registerRequest should add request data`() {
        // given
        val requestName = "testRequest"
        val data = mapOf("key" to "value")
        // when
        apiRequestsDumper.registerRequest(requestName, data)
        val result = apiRequestsDumper.dumpRequestByName(requestName)
        // then
        val expected = "Request: testRequest. Count: 1\n" +
            "Call 0. Time: $mockTimestamp. Params: key - value\n"
        result shouldBeEqualTo expected
    }

    @Test
    fun `dumpRequestByName should return not found for unknown request`() {
        // when
        val result = apiRequestsDumper.dumpRequestByName("unknownRequest")
        // then
        result shouldBeEqualTo "not found"
    }

    @Test
    fun `dumpAll should return all registered requests`() {
        // given
        val requestName1 = "testRequest1"
        val requestName2 = "testRequest2"
        val data1 = mapOf("key1" to "value1")
        val data2 = mapOf("key2" to "value2")
        // when
        apiRequestsDumper.registerRequest(requestName1, data1)
        apiRequestsDumper.registerRequest(requestName2, data2)
        val result = apiRequestsDumper.dumpAll()
        // then
        val expected = "Request: testRequest1. Count: 1\n" +
            "Call 0. Time: $mockTimestamp. Params: key1 - value1\n" +
            "\n" +
            "Request: testRequest2. Count: 1\n" +
            "Call 0. Time: $mockTimestamp. Params: key2 - value2\n\n"
        result shouldBeEqualTo expected
    }

    @Test
    fun `clearAll should remove all registered requests`() {
        val requestName = "testRequest"
        val data = mapOf("key" to "value")
        // when
        apiRequestsDumper.registerRequest(requestName, data)
        apiRequestsDumper.clearAll()
        val result = apiRequestsDumper.dumpAll()
        // then
        result shouldBeEqualTo ""
    }

    @Test
    fun `clearRequestContaining should remove specific requests`() {
        // given
        val requestName1 = "testRequest1"
        val requestName2 = "testRequest2"
        val data1 = mapOf("key1" to "value1")
        val data2 = mapOf("key2" to "value2")
        // when
        apiRequestsDumper.registerRequest(requestName1, data1)
        apiRequestsDumper.registerRequest(requestName2, data2)
        apiRequestsDumper.clearRequestContaining("testRequest1")
        val result = apiRequestsDumper.dumpAll()
        // then
        val expected = "Request: testRequest2. Count: 1\n" +
            "Call 0. Time: 15:21:44.005. Params: key2 - value2\n\n"
        result shouldBeEqualTo expected
    }

    @Test
    fun `countRequestContaining should return correct count`() {
        // given
        val requestName = "testRequest"
        val data = mapOf("key" to "value")
        // when
        apiRequestsDumper.registerRequest(requestName, data)
        apiRequestsDumper.registerRequest(requestName, data)
        val count = apiRequestsDumper.countRequestContaining(requestName)
        // then
        count shouldBeEqualTo 2
    }

    @Test
    fun `countAllRequests should return correct total count`() {
        val requestName1 = "testRequest1"
        val requestName2 = "testRequest2"
        val data1 = mapOf("key1" to "value1")
        val data2 = mapOf("key2" to "value2")

        apiRequestsDumper.registerRequest(requestName1, data1)
        apiRequestsDumper.registerRequest(requestName2, data2)

        val count = apiRequestsDumper.countAllRequests()
        count shouldBeEqualTo 2
    }
}
