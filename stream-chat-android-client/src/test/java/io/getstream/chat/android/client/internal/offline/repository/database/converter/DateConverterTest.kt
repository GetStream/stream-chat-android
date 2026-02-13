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

package io.getstream.chat.android.client.internal.offline.repository.database.converter

import io.getstream.chat.android.client.internal.offline.repository.database.converter.internal.DateConverter
import org.amshove.kluent.shouldBeEqualTo
import org.amshove.kluent.shouldBeNull
import org.junit.Test
import java.util.Date

internal class DateConverterTest {
    @Test
    fun testNullEncoding() {
        val converter = DateConverter()
        val output = converter.dateToTimestamp(null)
        val converted = converter.fromTimestamp(output)
        converted.shouldBeNull()
    }

    @Test
    fun testSortEncoding() {
        val converter = DateConverter()
        val date = Date()
        val output = converter.dateToTimestamp(date)
        val converted = converter.fromTimestamp(output)
        converted!! shouldBeEqualTo date
    }
}
