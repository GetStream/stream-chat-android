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

import io.getstream.chat.android.client.internal.offline.repository.database.converter.internal.LocationConverter
import io.getstream.chat.android.client.internal.offline.repository.domain.message.internal.LocationEntity
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource

internal class LocationConverterTest {

    private val converter = LocationConverter()

    @ParameterizedTest
    @MethodSource("stringToLocation")
    fun testStringToLocation(input: String?, expected: LocationEntity?) {
        assertEquals(expected, converter.stringToLocation(input))
    }

    @ParameterizedTest
    @MethodSource("locationToString")
    fun testLocationToString(input: LocationEntity?, expected: String?) {
        assertEquals(expected, converter.locationToString(input))
    }

    @ParameterizedTest
    @MethodSource("stringToLocationList")
    fun testStringToLocationList(input: String?, expected: List<LocationEntity>?) {
        assertEquals(expected, converter.stringToLocationList(input))
    }

    @ParameterizedTest
    @MethodSource("locationListToString")
    fun testLocationListToString(input: List<LocationEntity>?, expected: String?) {
        assertEquals(expected, converter.locationListToString(input))
    }

    companion object {
        @JvmStatic
        fun stringToLocation() = listOf(
            arrayOf<Any?>(null, null),
            arrayOf(
                "{\"latitude\":12.34,\"longitude\":56.78}",
                LocationEntity(latitude = 12.34, longitude = 56.78),
            ),
        )

        @JvmStatic
        fun locationToString() = listOf(
            arrayOf<Any?>(null, null),
            arrayOf(
                LocationEntity(),
                "{\"cid\":\"\",\"messageId\":\"\",\"userId\":\"\"," +
                    "\"latitude\":0.0,\"longitude\":0.0,\"deviceId\":\"\"}",
            ),
        )

        @JvmStatic
        fun stringToLocationList() = listOf(
            arrayOf(null, emptyList<LocationEntity>()),
            arrayOf("", emptyList<LocationEntity>()),
            arrayOf("null", emptyList<LocationEntity>()),
            arrayOf(
                "[{\"latitude\":12.34,\"longitude\":56.78}]",
                listOf(LocationEntity(latitude = 12.34, longitude = 56.78)),
            ),
        )

        @JvmStatic
        fun locationListToString() = listOf(
            arrayOf<Any?>(null, null),
            arrayOf(
                listOf(LocationEntity()),
                "[{\"cid\":\"\",\"messageId\":\"\",\"userId\":\"\"," +
                    "\"latitude\":0.0,\"longitude\":0.0,\"deviceId\":\"\"}]",
            ),
        )
    }
}
