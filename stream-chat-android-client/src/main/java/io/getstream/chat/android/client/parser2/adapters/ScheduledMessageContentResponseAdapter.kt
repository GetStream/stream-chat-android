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
import io.getstream.chat.android.network.models.ScheduledMessageContentResponse

/**
 * Collects the custom fields of a scheduled message's content into `custom`, whether they arrive nested under
 * `custom` or at the root of the content.
 */
internal object ScheduledMessageContentResponseAdapter :
    CustomObjectDtoAdapter<ScheduledMessageContentResponse>(
        ScheduledMessageContentResponse::class,
        extraDataPropertyName = "custom",
        mergesNestedExtraData = true,
    ) {

    @FromJson
    fun fromJson(
        jsonReader: JsonReader,
        mapAdapter: JsonAdapter<MutableMap<String, Any>>,
        valueAdapter: JsonAdapter<ScheduledMessageContentResponse>,
    ): ScheduledMessageContentResponse? = parseWithExtraData(jsonReader, mapAdapter, valueAdapter)

    @ToJson
    @Suppress("UNUSED_PARAMETER")
    fun toJson(jsonWriter: JsonWriter, value: ScheduledMessageContentResponse): Unit =
        error("Can't convert this to Json")
}
