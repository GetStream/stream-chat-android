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

package io.getstream.chat.android.client.internal.offline.repository.database.database.converter.internal

import androidx.room.TypeConverter
import com.squareup.moshi.adapter
import io.getstream.chat.android.client.internal.offline.repository.domain.channel.userread.internal.ChannelUserReadEntity
import io.getstream.chat.android.client.internal.offline.repository.domain.user.internal.UserMuteEntity

@OptIn(ExperimentalStdlibApi::class)
internal class ListConverter {

    private val stringListAdapter = moshi.adapter<List<String>>()
    private val channelUserReadListAdapter = moshi.adapter<List<ChannelUserReadEntity>>()
    private val userMuteListAdapter = moshi.adapter<List<UserMuteEntity>>()

    @TypeConverter
    fun toStringList(data: String?): List<String>? {
        if (data.isNullOrEmpty() || data == "null") {
            return emptyList()
        }
        return stringListAdapter.fromJson(data)
    }

    @TypeConverter
    fun fromStringList(strings: List<String>?): String? =
        stringListAdapter.toJson(strings)

    @TypeConverter
    fun toReadList(data: String?): List<ChannelUserReadEntity>? {
        if (data.isNullOrEmpty() || data == "null") {
            return emptyList()
        }
        return channelUserReadListAdapter.fromJson(data)
    }

    @TypeConverter
    fun fromReadList(entities: List<ChannelUserReadEntity>?): String? =
        channelUserReadListAdapter.toJson(entities)

    @TypeConverter
    fun toUserMuteList(data: String?): List<UserMuteEntity>? {
        if (data.isNullOrEmpty() || data == "null") {
            return emptyList()
        }
        return userMuteListAdapter.fromJson(data)
    }

    @TypeConverter
    fun fromUserMuteList(entities: List<UserMuteEntity>?): String? =
        userMuteListAdapter.toJson(entities)
}
