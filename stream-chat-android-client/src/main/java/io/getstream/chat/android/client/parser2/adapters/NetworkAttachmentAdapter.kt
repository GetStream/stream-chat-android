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
import io.getstream.chat.android.network.models.Attachment

/**
 * Wire keys the generated model declares and the hand-written attachment DTO did not, so they reached
 * `Attachment.extraData` and would otherwise stop doing so. `giphy` is among them, which is where
 * `Attachment.giphyInfo()` reads gif urls: keeping the raw object preserves the exact map the UI already
 * reads, rather than rebuilding it from the typed field. Drop with AND-1398.
 */
internal val GENERATED_ATTACHMENT_EXTRA_DATA_KEYS = setOf(
    "author_icon",
    "color",
    "footer",
    "footer_icon",
    "pretext",
    "actions",
    "fields",
    "giphy",
)

// The generated Attachment carries custom data in a `custom` field that must be flattened to the
// JSON root on the wire; extraDataPropertyName matches its @Json(name = "custom"). The same model is
// both the request and the response body, so this adapter handles both directions.
internal object NetworkAttachmentAdapter : CustomObjectDtoAdapter<Attachment>(
    Attachment::class,
    extraDataPropertyName = "custom",
    alsoKeepInExtraData = GENERATED_ATTACHMENT_EXTRA_DATA_KEYS,
) {

    @FromJson
    fun fromJson(
        jsonReader: JsonReader,
        mapAdapter: JsonAdapter<MutableMap<String, Any>>,
        valueAdapter: JsonAdapter<Attachment>,
    ): Attachment? = parseWithExtraData(jsonReader, mapAdapter, valueAdapter)

    @ToJson
    fun toJson(
        jsonWriter: JsonWriter,
        request: Attachment?,
        mapAdapter: JsonAdapter<MutableMap<String, Any?>>,
        requestAdapter: JsonAdapter<Attachment>,
    ) = serializeWithExtraData(jsonWriter, request, mapAdapter, requestAdapter)
}
