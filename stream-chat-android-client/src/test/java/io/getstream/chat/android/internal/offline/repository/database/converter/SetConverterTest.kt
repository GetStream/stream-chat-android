/*
 * Copyright (c) 2014-2022 Stream.io Inc. All rights reserved.
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

package io.getstream.chat.android.internal.offline.repository.database.converter

import io.getstream.chat.android.internal.offline.repository.database.database.converter.internal.SetConverter
import org.amshove.kluent.shouldBeEqualTo
import org.junit.Test

internal class SetConverterTest {
    @Test
    fun testNullEncoding() {
        val converter = SetConverter()
        val output = converter.sortedSetToString(null)
        val converted = converter.stringToSortedSet(output)
        converted shouldBeEqualTo sortedSetOf()
    }

    @Test
    fun testSortEncoding() {
        val converter = SetConverter()
        val colors = mutableSetOf("green", "blue")
        val output = converter.sortedSetToString(colors)
        val converted = converter.stringToSortedSet(output)
        converted shouldBeEqualTo colors
    }
}
