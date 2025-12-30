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

package io.getstream.chat.android.internal.offline.repository.database.database.converter.internal

import androidx.room.TypeConverter
import com.squareup.moshi.adapter
import io.getstream.chat.android.internal.offline.repository.domain.channel.member.internal.MemberEntity
import io.getstream.chat.android.internal.offline.repository.domain.channel.userread.internal.ChannelUserReadEntity

internal class MapConverter {
    @OptIn(ExperimentalStdlibApi::class)
    private val stringMapAdapter = moshi.adapter<Map<String, String>>()

    @OptIn(ExperimentalStdlibApi::class)
    private val intMapAdapter = moshi.adapter<Map<String, Int>>()

    @OptIn(ExperimentalStdlibApi::class)
    private val channelUserReadMapAdapter = moshi.adapter<Map<String, ChannelUserReadEntity>>()

    @OptIn(ExperimentalStdlibApi::class)
    private val memberEntityMapAdapter = moshi.adapter<Map<String, MemberEntity>>()

    @TypeConverter
    fun readMapToString(someObjects: Map<String, ChannelUserReadEntity>?): String? {
        return channelUserReadMapAdapter.toJson(someObjects)
    }

    @TypeConverter
    fun stringToReadMap(data: String?): Map<String, ChannelUserReadEntity>? {
        if (data.isNullOrEmpty() || data == "null") {
            return mutableMapOf()
        }
        return channelUserReadMapAdapter.fromJson(data)
    }

    @TypeConverter
    fun memberMapToString(someObjects: Map<String, MemberEntity>?): String? {
        return memberEntityMapAdapter.toJson(someObjects)
    }

    @TypeConverter
    fun stringToMemberMap(data: String?): Map<String, MemberEntity>? {
        if (data.isNullOrEmpty() || data == "null") {
            return emptyMap()
        }
        return memberEntityMapAdapter.fromJson(data)
    }

    @TypeConverter
    fun stringToMap(data: String?): Map<String, Int>? {
        if (data.isNullOrEmpty() || data == "null") {
            return mutableMapOf()
        }
        return intMapAdapter.fromJson(data)
    }

    @TypeConverter
    fun mapToString(someObjects: Map<String, Int>?): String? {
        return intMapAdapter.toJson(someObjects)
    }

    @TypeConverter
    fun stringToStringMap(data: String?): Map<String, String>? {
        if (data.isNullOrEmpty() || data == "null") {
            return mutableMapOf()
        }
        return stringMapAdapter.fromJson(data)
    }

    @TypeConverter
    fun stringMapToString(someObjects: Map<String, String>?): String? {
        return stringMapAdapter.toJson(someObjects)
    }
}
