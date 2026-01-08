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

package io.getstream.chat.android.client.parser2

import io.getstream.chat.android.client.parser2.adapters.DateAdapter
import org.amshove.kluent.shouldBeEqualTo
import org.amshove.kluent.shouldBeNull
import org.junit.jupiter.api.Test
import java.util.Date

internal class DateAdapterTest {

    private val dateAdapter = DateAdapter()

    @Test
    fun readValidDates() {
        dateAdapter.fromJson("\"2020-06-29T06:14:28Z\"")!!.time shouldBeEqualTo 1593411268000
        dateAdapter.fromJson("\"2020-06-29T06:14:28.0Z\"")!!.time shouldBeEqualTo 1593411268000
        dateAdapter.fromJson("\"2020-06-29T06:14:28.00Z\"")!!.time shouldBeEqualTo 1593411268000
        dateAdapter.fromJson("\"2020-06-29T06:14:28.000Z\"")!!.time shouldBeEqualTo 1593411268000
        dateAdapter.fromJson("\"2020-06-29T06:14:28.100Z\"")!!.time shouldBeEqualTo 1593411268100
    }

    @Test
    fun readEmptyDate() {
        dateAdapter.fromJson("\"\"").shouldBeNull()
    }

    @Test
    fun readNullDate() {
        dateAdapter.fromJson("null").shouldBeNull()
    }

    @Test
    fun readNonsenseDate() {
        dateAdapter.fromJson("\"bla bla bla\"").shouldBeNull()
    }

    @Test
    fun writeValidDate() {
        val result = dateAdapter.toJson(Date(1593411268000))
        result shouldBeEqualTo "\"2020-06-29T06:14:28.000Z\""
    }

    @Test
    fun writeNullValue() {
        val result = dateAdapter.toJson(null)
        result shouldBeEqualTo "null"
    }
}
