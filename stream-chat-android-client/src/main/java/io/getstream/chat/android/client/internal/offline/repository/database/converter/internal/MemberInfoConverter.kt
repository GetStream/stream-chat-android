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
import io.getstream.chat.android.client.internal.offline.repository.domain.message.internal.MemberInfoEntity

/**
 * Converter class defining how the [MemberInfoEntity] is stored in the database.
 */
internal class MemberInfoConverter {

    @OptIn(ExperimentalStdlibApi::class)
    private val adapter = moshi.adapter<MemberInfoEntity>()

    /**
     * Converts a [String] to a [MemberInfoEntity].
     */
    @TypeConverter
    fun stringToMemberInfo(data: String?): MemberInfoEntity? {
        if (data.isNullOrEmpty() || data == "null") {
            return null
        }
        return adapter.fromJson(data)
    }

    /**
     * Converts a [MemberInfoEntity] to a [String].
     */
    @TypeConverter
    fun memberInfoToString(memberInfo: MemberInfoEntity?): String? {
        return memberInfo?.let(adapter::toJson)
    }
}
