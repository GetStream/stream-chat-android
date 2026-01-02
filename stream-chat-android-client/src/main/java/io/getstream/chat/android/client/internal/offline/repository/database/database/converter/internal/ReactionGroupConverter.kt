/*
 * Copyright (c) 2014-2024 Stream.io Inc. All rights reserved.
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
import io.getstream.chat.android.client.internal.offline.repository.domain.message.internal.ReactionGroupEntity

internal class ReactionGroupConverter {

    @OptIn(ExperimentalStdlibApi::class)
    private val entityAdapter = moshi.adapter<ReactionGroupEntity>()

    @OptIn(ExperimentalStdlibApi::class)
    private val entityMapAdapter = moshi.adapter<Map<String, ReactionGroupEntity>>()

    @TypeConverter
    fun stringToReactionGroupEntity(data: String?): ReactionGroupEntity? {
        return data?.let {
            entityAdapter.fromJson(it)
        }
    }

    @TypeConverter
    fun reactionGroupEntityToString(entity: ReactionGroupEntity?): String? {
        return entity?.let {
            entityAdapter.toJson(it)
        }
    }

    @TypeConverter
    fun reactionGroupEntityMapToString(entities: Map<String, ReactionGroupEntity>?): String? {
        return entities?.let {
            entityMapAdapter.toJson(it)
        }
    }

    @TypeConverter
    fun stringToReactionGroupEntityMap(data: String?): Map<String, ReactionGroupEntity>? {
        if (data.isNullOrEmpty() || data == "null") {
            return mutableMapOf()
        }
        return entityMapAdapter.fromJson(data)
    }
}
