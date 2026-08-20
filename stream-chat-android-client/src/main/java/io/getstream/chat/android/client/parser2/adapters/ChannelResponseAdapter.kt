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
import io.getstream.chat.android.network.models.ChannelResponse

/**
 * Keys `ChannelResponse` declares that `DownstreamChannelDto` did not, so they used to reach
 * `Channel.extraData` and would otherwise stop doing so. Kept there as well as mapped, matching how
 * [LEGACY_CHANNEL_EXTRA_DATA_KEYS] treats the keys the hand-written DTO declared. Drop with AND-1398.
 */
internal val GENERATED_CHANNEL_EXTRA_DATA_KEYS = setOf(
    "auto_translation_enabled",
    "auto_translation_language",
    "hidden",
    "hide_messages_before",
    "mute_expires_at",
    "muted",
    "truncated_by",
)

// Downstream (read-only) adapter for the generated ChannelResponse: collects root-level custom fields
// into `custom`, matching the wire's flattened extra data. extraDataPropertyName is its @Json name.
internal object ChannelResponseAdapter :
    CustomObjectDtoAdapter<ChannelResponse>(
        ChannelResponse::class,
        extraDataPropertyName = "custom",
        alsoKeepInExtraData = LEGACY_CHANNEL_EXTRA_DATA_KEYS + GENERATED_CHANNEL_EXTRA_DATA_KEYS,
    ) {

    @FromJson
    fun fromJson(
        jsonReader: JsonReader,
        mapAdapter: JsonAdapter<MutableMap<String, Any>>,
        valueAdapter: JsonAdapter<ChannelResponse>,
    ): ChannelResponse? = parseWithExtraData(jsonReader, mapAdapter, valueAdapter)

    @ToJson
    @Suppress("UNUSED_PARAMETER")
    fun toJson(jsonWriter: JsonWriter, value: ChannelResponse): Unit = error("Can't convert this to Json")
}
