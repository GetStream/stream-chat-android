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

package io.getstream.chat.android.client.parser2.adapters

import com.squareup.moshi.FromJson
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.JsonReader
import com.squareup.moshi.JsonWriter
import com.squareup.moshi.ToJson
import io.getstream.chat.android.network.models.UserResponseCommonFields
import io.getstream.chat.android.network.models.UserResponsePrivacyFields

/**
 * Keys the event user models declare that `DownstreamUserDto` did not, so they used to reach
 * `User.extraData` and would otherwise stop doing so. The domain has no property for either, so they are
 * kept in the overflow map. Drop with AND-1398.
 */
internal val GENERATED_EVENT_USER_EXTRA_DATA_KEYS = setOf(
    "deleted_at",
    "revoke_tokens_issued_before",
)

// Downstream (read-only) adapters for the user shapes user events carry: collect root-level custom
// fields into `custom`, matching the wire's flattened extra data.
internal object UserResponseCommonFieldsAdapter :
    CustomObjectDtoAdapter<UserResponseCommonFields>(
        UserResponseCommonFields::class,
        extraDataPropertyName = "custom",
        alsoKeepInExtraData = GENERATED_EVENT_USER_EXTRA_DATA_KEYS,
    ) {

    @FromJson
    fun fromJson(
        jsonReader: JsonReader,
        mapAdapter: JsonAdapter<MutableMap<String, Any>>,
        valueAdapter: JsonAdapter<UserResponseCommonFields>,
    ): UserResponseCommonFields? = parseWithExtraData(jsonReader, mapAdapter, valueAdapter)

    @ToJson
    fun toJson(jsonWriter: JsonWriter, value: UserResponseCommonFields?) {
        if (value != null) error("Can't convert this to Json")
        jsonWriter.nullValue()
    }
}

internal object UserResponsePrivacyFieldsAdapter :
    CustomObjectDtoAdapter<UserResponsePrivacyFields>(
        UserResponsePrivacyFields::class,
        extraDataPropertyName = "custom",
        alsoKeepInExtraData = GENERATED_EVENT_USER_EXTRA_DATA_KEYS,
    ) {

    @FromJson
    fun fromJson(
        jsonReader: JsonReader,
        mapAdapter: JsonAdapter<MutableMap<String, Any>>,
        valueAdapter: JsonAdapter<UserResponsePrivacyFields>,
    ): UserResponsePrivacyFields? = parseWithExtraData(jsonReader, mapAdapter, valueAdapter)

    @ToJson
    fun toJson(jsonWriter: JsonWriter, value: UserResponsePrivacyFields?) {
        if (value != null) error("Can't convert this to Json")
        jsonWriter.nullValue()
    }
}
