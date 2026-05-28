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

package io.getstream.chat.android.client.internal.offline.repository.database.converter.internal

import androidx.room.TypeConverter
import com.squareup.moshi.adapter

/**
 * Type converter for nullable `Map<String, Any>?` columns. Unlike [ExtraDataConverter] (which
 * coerces `null` to an empty map), this converter round-trips `null` faithfully so callers can
 * distinguish "absent" from "empty".
 *
 * Apply at field level via `@field:TypeConverters(NullableMapConverter::class)` on the columns
 * that need null-preserving semantics.
 */
internal class NullableMapConverter {
    @OptIn(ExperimentalStdlibApi::class)
    private val adapter = moshi.adapter<Map<String, Any>>()

    @TypeConverter
    fun stringToMap(data: String?): Map<String, Any>? {
        if (data == null || data.isEmpty() || data == "null") return null
        return adapter.fromJson(data)
    }

    @TypeConverter
    fun mapToString(map: Map<String, Any>?): String? = map?.let(adapter::toJson)
}
