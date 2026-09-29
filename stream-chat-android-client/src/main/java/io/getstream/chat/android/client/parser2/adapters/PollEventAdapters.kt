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
import io.getstream.chat.android.network.models.PollClosedEvent
import io.getstream.chat.android.network.models.PollDeletedEvent
import io.getstream.chat.android.network.models.PollUpdatedEvent
import io.getstream.chat.android.network.models.PollVoteCastedEvent
import io.getstream.chat.android.network.models.PollVoteChangedEvent
import io.getstream.chat.android.network.models.PollVoteRemovedEvent

// Downstream (read-only) adapters for the generated poll events: collect the root-level custom fields into
// `custom`, matching the wire's flattened extra data. extraDataPropertyName is their @Json name.

internal object PollClosedEventAdapter :
    CustomObjectDtoAdapter<PollClosedEvent>(PollClosedEvent::class, extraDataPropertyName = "custom") {

    @FromJson
    fun fromJson(
        jsonReader: JsonReader,
        mapAdapter: JsonAdapter<MutableMap<String, Any>>,
        valueAdapter: JsonAdapter<PollClosedEvent>,
    ): PollClosedEvent? = parseWithExtraData(jsonReader, mapAdapter, valueAdapter)

    @ToJson
    fun toJson(jsonWriter: JsonWriter, value: PollClosedEvent): Unit = error("Can't convert this to Json")
}

internal object PollDeletedEventAdapter :
    CustomObjectDtoAdapter<PollDeletedEvent>(PollDeletedEvent::class, extraDataPropertyName = "custom") {

    @FromJson
    fun fromJson(
        jsonReader: JsonReader,
        mapAdapter: JsonAdapter<MutableMap<String, Any>>,
        valueAdapter: JsonAdapter<PollDeletedEvent>,
    ): PollDeletedEvent? = parseWithExtraData(jsonReader, mapAdapter, valueAdapter)

    @ToJson
    fun toJson(jsonWriter: JsonWriter, value: PollDeletedEvent): Unit = error("Can't convert this to Json")
}

internal object PollUpdatedEventAdapter :
    CustomObjectDtoAdapter<PollUpdatedEvent>(PollUpdatedEvent::class, extraDataPropertyName = "custom") {

    @FromJson
    fun fromJson(
        jsonReader: JsonReader,
        mapAdapter: JsonAdapter<MutableMap<String, Any>>,
        valueAdapter: JsonAdapter<PollUpdatedEvent>,
    ): PollUpdatedEvent? = parseWithExtraData(jsonReader, mapAdapter, valueAdapter)

    @ToJson
    fun toJson(jsonWriter: JsonWriter, value: PollUpdatedEvent): Unit = error("Can't convert this to Json")
}

internal object PollVoteCastedEventAdapter :
    CustomObjectDtoAdapter<PollVoteCastedEvent>(PollVoteCastedEvent::class, extraDataPropertyName = "custom") {

    @FromJson
    fun fromJson(
        jsonReader: JsonReader,
        mapAdapter: JsonAdapter<MutableMap<String, Any>>,
        valueAdapter: JsonAdapter<PollVoteCastedEvent>,
    ): PollVoteCastedEvent? = parseWithExtraData(jsonReader, mapAdapter, valueAdapter)

    @ToJson
    fun toJson(jsonWriter: JsonWriter, value: PollVoteCastedEvent): Unit = error("Can't convert this to Json")
}

internal object PollVoteChangedEventAdapter :
    CustomObjectDtoAdapter<PollVoteChangedEvent>(PollVoteChangedEvent::class, extraDataPropertyName = "custom") {

    @FromJson
    fun fromJson(
        jsonReader: JsonReader,
        mapAdapter: JsonAdapter<MutableMap<String, Any>>,
        valueAdapter: JsonAdapter<PollVoteChangedEvent>,
    ): PollVoteChangedEvent? = parseWithExtraData(jsonReader, mapAdapter, valueAdapter)

    @ToJson
    fun toJson(jsonWriter: JsonWriter, value: PollVoteChangedEvent): Unit = error("Can't convert this to Json")
}

internal object PollVoteRemovedEventAdapter :
    CustomObjectDtoAdapter<PollVoteRemovedEvent>(PollVoteRemovedEvent::class, extraDataPropertyName = "custom") {

    @FromJson
    fun fromJson(
        jsonReader: JsonReader,
        mapAdapter: JsonAdapter<MutableMap<String, Any>>,
        valueAdapter: JsonAdapter<PollVoteRemovedEvent>,
    ): PollVoteRemovedEvent? = parseWithExtraData(jsonReader, mapAdapter, valueAdapter)

    @ToJson
    fun toJson(jsonWriter: JsonWriter, value: PollVoteRemovedEvent): Unit = error("Can't convert this to Json")
}
