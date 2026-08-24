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
import io.getstream.chat.android.network.models.FullUserResponse

/**
 * Wire keys the generated model declares and the hand-written user DTO did not, so they reached
 * `User.extraData` and would otherwise stop doing so. Drop with AND-1398.
 */
internal val GENERATED_FULL_USER_EXTRA_DATA_KEYS = setOf(
    "shadow_banned",
    "ban_expires",
    "deleted_at",
    "revoke_tokens_issued_before",
    "latest_hidden_channels",
)

// Downstream (read-only) adapter for the generated FullUserResponse: collects root-level custom fields
// into `custom`, matching the wire's flattened extra data.
internal object FullUserResponseAdapter : CustomObjectDtoAdapter<FullUserResponse>(
    FullUserResponse::class,
    extraDataPropertyName = "custom",
    alsoKeepInExtraData = GENERATED_FULL_USER_EXTRA_DATA_KEYS,
) {

    @FromJson
    fun fromJson(
        jsonReader: JsonReader,
        mapAdapter: JsonAdapter<MutableMap<String, Any>>,
        valueAdapter: JsonAdapter<FullUserResponse>,
    ): FullUserResponse? = parseWithExtraData(jsonReader, mapAdapter, valueAdapter)

    @ToJson
    fun toJson(jsonWriter: JsonWriter, value: FullUserResponse?) {
        error("Can't convert this to Json")
    }
}
