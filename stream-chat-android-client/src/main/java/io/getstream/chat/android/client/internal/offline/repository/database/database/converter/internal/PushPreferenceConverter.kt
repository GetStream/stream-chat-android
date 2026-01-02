/*
 * Copyright (c) 2014-2025 Stream.io Inc. All rights reserved.
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
import io.getstream.chat.android.client.internal.offline.repository.domain.push.internal.PushPreferenceEntity

/**
 * Converter for [PushPreferenceEntity] to and from JSON.
 */
internal class PushPreferenceConverter {

    @OptIn(ExperimentalStdlibApi::class)
    private val pushPreferenceAdapter = moshi.adapter<PushPreferenceEntity>()

    /**
     * Converts a [String] to a [PushPreferenceEntity].
     */
    @TypeConverter
    fun stringToPushPreference(data: String?): PushPreferenceEntity? {
        return data?.let {
            pushPreferenceAdapter.fromJson(it)
        }
    }

    /**
     * Converts a [PushPreferenceEntity] to a [String].
     */
    @TypeConverter
    fun pushPreferenceToString(entity: PushPreferenceEntity?): String? {
        return entity?.let {
            pushPreferenceAdapter.toJson(it)
        }
    }
}
