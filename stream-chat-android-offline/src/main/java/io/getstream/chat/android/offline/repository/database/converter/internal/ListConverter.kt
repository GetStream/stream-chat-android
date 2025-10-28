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
import io.getstream.chat.android.offline.repository.domain.channel.userread.internal.ChannelUserReadEntity

internal class ListConverter {
    @OptIn(ExperimentalStdlibApi::class)
    private val adapter = moshi.adapter<List<String>>()

    @OptIn(ExperimentalStdlibApi::class)
    private val channelUserReadListAdapter = moshi.adapter<List<ChannelUserReadEntity>>()

    @TypeConverter
    fun stringToStringList(data: String?): List<String>? {
        if (data.isNullOrEmpty() || data == "null") {
            return emptyList()
        }
        return adapter.fromJson(data)
    }

    @TypeConverter
    fun stringListToString(someObjects: List<String>?): String? = adapter.toJson(someObjects)

    @TypeConverter
    fun readListToString(data: List<ChannelUserReadEntity>?): String? = channelUserReadListAdapter.toJson(data)

    @TypeConverter
    fun stringToReadList(data: String?): List<ChannelUserReadEntity>? {
        if (data.isNullOrEmpty() || data == "null") {
            return emptyList()
        }
        return channelUserReadListAdapter.fromJson(data)
    }
}
