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
import io.getstream.chat.android.network.models.ChannelMemberResponse

/**
 * Keys `ChannelMemberResponse` declares that `DownstreamMemberDto` did not, so they used to reach
 * `Member.extraData` and would otherwise stop doing so. Only `user_id` is read at all, as the fallback
 * for the user id when the payload carries no user, and that does not restore the map an app may already
 * read. Drop with AND-1398.
 */
internal val GENERATED_MEMBER_EXTRA_DATA_KEYS = setOf(
    "deleted_at",
    "deleted_messages",
    "is_moderator",
    "role",
    "user_id",
)

// Downstream (read-only) adapter for the generated ChannelMemberResponse: collects root-level custom fields
// into `custom`, matching the wire's flattened extra data. extraDataPropertyName is its @Json name.
internal object ChannelMemberResponseAdapter :
    CustomObjectDtoAdapter<ChannelMemberResponse>(
        ChannelMemberResponse::class,
        extraDataPropertyName = "custom",
        alsoKeepInExtraData = GENERATED_MEMBER_EXTRA_DATA_KEYS,
    ) {

    @FromJson
    fun fromJson(
        jsonReader: JsonReader,
        mapAdapter: JsonAdapter<MutableMap<String, Any>>,
        valueAdapter: JsonAdapter<ChannelMemberResponse>,
    ): ChannelMemberResponse? = parseWithExtraData(jsonReader, mapAdapter, valueAdapter)

    @ToJson
    @Suppress("UNUSED_PARAMETER")
    fun toJson(jsonWriter: JsonWriter, value: ChannelMemberResponse): Unit = error("Can't convert this to Json")
}
