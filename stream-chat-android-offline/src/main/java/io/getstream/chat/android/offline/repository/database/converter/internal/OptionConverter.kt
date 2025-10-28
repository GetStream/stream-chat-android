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
import io.getstream.chat.android.offline.repository.domain.message.internal.OptionEntity

internal class OptionConverter {

    @OptIn(ExperimentalStdlibApi::class)
    private val entityAdapter = moshi.adapter<OptionEntity>()

    @OptIn(ExperimentalStdlibApi::class)
    private val entityListAdapter = moshi.adapter<List<OptionEntity>>()

    @TypeConverter
    fun stringToOption(data: String?): OptionEntity? = data?.let {
        entityAdapter.fromJson(it)
    }

    @TypeConverter
    fun optionToString(entity: OptionEntity?): String? = entity?.let {
        entityAdapter.toJson(it)
    }

    @TypeConverter
    fun stringToOptionList(data: String?): List<OptionEntity>? {
        if (data.isNullOrEmpty() || data == "null") {
            return emptyList()
        }
        return entityListAdapter.fromJson(data)
    }

    @TypeConverter
    fun optionListToString(entities: List<OptionEntity>?): String? = entities?.let {
        entityListAdapter.toJson(it)
    }
}
