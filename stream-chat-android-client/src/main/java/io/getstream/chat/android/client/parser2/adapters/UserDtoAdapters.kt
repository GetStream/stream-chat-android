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
import io.getstream.chat.android.client.api2.model.dto.DownstreamUserDto
import io.getstream.chat.android.client.api2.model.dto.UpstreamUserDto
import io.getstream.chat.android.network.models.OwnUserResponse
import io.getstream.chat.android.network.models.UserResponseCommonFields
import io.getstream.chat.android.network.models.UserResponsePrivacyFields

internal object DownstreamUserDtoAdapter :
    CustomObjectDtoAdapter<DownstreamUserDto>(
        kClass = DownstreamUserDto::class,
        extraDataPropertyName = "custom",
    ) {

    @FromJson
    fun fromJson(
        jsonReader: JsonReader,
        mapAdapter: JsonAdapter<MutableMap<String, Any>>,
        userAdapter: JsonAdapter<DownstreamUserDto>,
    ): DownstreamUserDto? = parseWithExtraData(jsonReader, mapAdapter, userAdapter)

    @ToJson
    fun toJson(
        jsonWriter: JsonWriter,
        value: DownstreamUserDto?,
        mapAdapter: JsonAdapter<MutableMap<String, Any?>>,
        userAdapter: JsonAdapter<DownstreamUserDto>,
    ) = serializeWithExtraData(jsonWriter, value, mapAdapter, userAdapter)
}

internal object OwnUserResponseAdapter :
    CustomObjectDtoAdapter<OwnUserResponse>(
        kClass = OwnUserResponse::class,
        extraDataPropertyName = "custom",
    ) {

    @FromJson
    fun fromJson(
        jsonReader: JsonReader,
        mapAdapter: JsonAdapter<MutableMap<String, Any>>,
        userAdapter: JsonAdapter<OwnUserResponse>,
    ): OwnUserResponse? = parseWithExtraData(jsonReader, mapAdapter, userAdapter)

    @ToJson
    fun toJson(
        jsonWriter: JsonWriter,
        value: OwnUserResponse?,
        mapAdapter: JsonAdapter<MutableMap<String, Any?>>,
        userAdapter: JsonAdapter<OwnUserResponse>,
    ) = serializeWithExtraData(jsonWriter, value, mapAdapter, userAdapter)
}

internal object UserResponsePrivacyFieldsAdapter :
    CustomObjectDtoAdapter<UserResponsePrivacyFields>(
        kClass = UserResponsePrivacyFields::class,
        extraDataPropertyName = "custom",
    ) {

    @FromJson
    fun fromJson(
        jsonReader: JsonReader,
        mapAdapter: JsonAdapter<MutableMap<String, Any>>,
        userAdapter: JsonAdapter<UserResponsePrivacyFields>,
    ): UserResponsePrivacyFields? = parseWithExtraData(jsonReader, mapAdapter, userAdapter)

    @ToJson
    fun toJson(
        jsonWriter: JsonWriter,
        value: UserResponsePrivacyFields?,
        mapAdapter: JsonAdapter<MutableMap<String, Any?>>,
        userAdapter: JsonAdapter<UserResponsePrivacyFields>,
    ) = serializeWithExtraData(jsonWriter, value, mapAdapter, userAdapter)
}

internal object UpstreamUserDtoAdapter :
    CustomObjectDtoAdapter<UpstreamUserDto>(
        kClass = UpstreamUserDto::class,
        extraDataPropertyName = "custom",
    ) {

    @FromJson
    fun fromJson(
        jsonReader: JsonReader,
        mapAdapter: JsonAdapter<MutableMap<String, Any>>,
        userAdapter: JsonAdapter<UpstreamUserDto>,
    ): UpstreamUserDto? = parseWithExtraData(jsonReader, mapAdapter, userAdapter)

    @ToJson
    fun toJson(
        jsonWriter: JsonWriter,
        value: UpstreamUserDto?,
        mapAdapter: JsonAdapter<MutableMap<String, Any?>>,
        userAdapter: JsonAdapter<UpstreamUserDto>,
    ) = serializeWithExtraData(jsonWriter, value, mapAdapter, userAdapter)
}

internal object UserResponseCommonFieldsAdapter :
    CustomObjectDtoAdapter<UserResponseCommonFields>(
        kClass = UserResponseCommonFields::class,
        extraDataPropertyName = "custom",
    ) {

    @FromJson
    fun fromJson(
        jsonReader: JsonReader,
        mapAdapter: JsonAdapter<MutableMap<String, Any>>,
        userAdapter: JsonAdapter<UserResponseCommonFields>,
    ): UserResponseCommonFields? = parseWithExtraData(jsonReader, mapAdapter, userAdapter)

    @ToJson
    fun toJson(
        jsonWriter: JsonWriter,
        value: UserResponseCommonFields?,
        mapAdapter: JsonAdapter<MutableMap<String, Any?>>,
        userAdapter: JsonAdapter<UserResponseCommonFields>,
    ) = serializeWithExtraData(jsonWriter, value, mapAdapter, userAdapter)
}
