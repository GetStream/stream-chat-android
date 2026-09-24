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
import io.getstream.chat.android.network.models.OwnUserResponse

/**
 * Keys `OwnUserResponse` declares that `DownstreamUserDto` did not, so they used to reach
 * `User.extraData` and would otherwise stop doing so. The domain has no property for any of them, so they
 * are kept in the overflow map rather than mapped. Drop with AND-1398.
 */
internal val GENERATED_OWN_USER_EXTRA_DATA_KEYS = setOf(
    "deleted_at",
    "latest_hidden_channels",
    "revoke_tokens_issued_before",
    "total_unread_count_by_team",
)

// Downstream (read-only) adapter for the generated OwnUserResponse: collects root-level custom fields
// into `custom`, matching the wire's flattened extra data.
internal object OwnUserResponseAdapter :
    CustomObjectDtoAdapter<OwnUserResponse>(
        OwnUserResponse::class,
        extraDataPropertyName = "custom",
        alsoKeepInExtraData = GENERATED_OWN_USER_EXTRA_DATA_KEYS,
    ) {

    @FromJson
    fun fromJson(
        jsonReader: JsonReader,
        mapAdapter: JsonAdapter<MutableMap<String, Any>>,
        valueAdapter: JsonAdapter<OwnUserResponse>,
    ): OwnUserResponse? = parseWithExtraData(jsonReader, mapAdapter, valueAdapter)

    // A nullable holder must serialize as an omitted field rather than tripping the non-null check.
    @ToJson
    fun toJson(jsonWriter: JsonWriter, value: OwnUserResponse?) {
        if (value != null) error("Can't convert this to Json")
        jsonWriter.nullValue()
    }
}
