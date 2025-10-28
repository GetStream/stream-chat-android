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

package io.getstream.chat.android.offline.repository.database.converter.internal

import androidx.room.TypeConverter
import com.squareup.moshi.adapter

internal class SetConverter {
    @OptIn(ExperimentalStdlibApi::class)
    private val adapter = moshi.adapter<Set<String>>()

    @TypeConverter
    fun stringToSortedSet(data: String?): Set<String> {
        if (data.isNullOrEmpty() || data == "null") {
            return setOf()
        }
        return adapter.fromJson(data) ?: emptySet()
    }

    /**
     * @return Nullable [String] to let KSP know this function cannot be used for "Nullable to NonNull" converting.
     *
     * An example of the incorrect behaviour:
     *     val stringifiedObject: String? = anotherConverter.objectToString(...)
     *     val stringList: Set<String> = SetConverter.stringToSortedSet(stringifiedObject)
     *     val nonNullStringifiedObject: String = SetConverter.sortedSetToString(stringifiedObject)
     *     // ... binding nonNullStringifiedObject to table's column
     *
     * The current behavior:
     *     val stringifiedObject: String? = anotherConverter.objectToString(...)
     *     // ... binding stringifiedObject to table's column
     */
    @TypeConverter
    fun sortedSetToString(someObjects: Set<String>?): String? = adapter.toJson(someObjects)
}
