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

package io.getstream.chat.android.client.parser2

import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import io.getstream.chat.android.client.api2.FlagRequestAdapterFactory
import io.getstream.chat.android.client.api2.MoshiUrlQueryPayloadFactory
import io.getstream.chat.android.client.api2.mapping.DtoMapping
import io.getstream.chat.android.client.api2.mapping.EventMapping
import io.getstream.chat.android.client.api2.mapping.toDomain
import io.getstream.chat.android.client.api2.model.dto.ChatEventDto
import io.getstream.chat.android.client.api2.model.dto.DownstreamUserDto
import io.getstream.chat.android.client.api2.model.dto.UnknownEventDto
import io.getstream.chat.android.client.api2.model.dto.UpstreamConnectedEventDto
import io.getstream.chat.android.client.api2.model.dto.utils.internal.ExactDate
import io.getstream.chat.android.client.api2.model.response.SocketErrorResponse
import io.getstream.chat.android.client.events.ChatEvent
import io.getstream.chat.android.client.events.ConnectedEvent
import io.getstream.chat.android.models.EventType
import io.getstream.chat.android.client.extensions.internal.enrichIfNeeded
import io.getstream.chat.android.client.parser.ChatParser
import io.getstream.chat.android.client.parser2.adapters.AttachmentDtoAdapter
import io.getstream.chat.android.client.parser2.adapters.CreatePollRequestAdapter
import io.getstream.chat.android.client.parser2.adapters.DownstreamChannelDtoAdapter
import io.getstream.chat.android.client.parser2.adapters.DownstreamMemberDtoAdapter
import io.getstream.chat.android.client.parser2.adapters.DownstreamMessageDtoAdapter
import io.getstream.chat.android.client.parser2.adapters.DownstreamPollDtoAdapter
import io.getstream.chat.android.client.parser2.adapters.DownstreamPollOptionDtoAdapter
import io.getstream.chat.android.client.parser2.adapters.DownstreamReactionDtoAdapter
import io.getstream.chat.android.client.parser2.adapters.DownstreamThreadDtoAdapter
import io.getstream.chat.android.client.parser2.adapters.DownstreamThreadInfoDtoAdapter
import io.getstream.chat.android.client.parser2.adapters.DownstreamUserDtoAdapter
import io.getstream.chat.android.client.parser2.adapters.EventAdapterFactory
import io.getstream.chat.android.client.parser2.adapters.ExactDateAdapter
import io.getstream.chat.android.client.parser2.adapters.OwnUserResponseAdapter
import io.getstream.chat.android.client.parser2.adapters.UpstreamMemberDataDtoAdapter
import io.getstream.chat.android.client.parser2.adapters.UpstreamMessageDtoAdapter
import io.getstream.chat.android.client.parser2.adapters.UpstreamOptionDtoAdapter
import io.getstream.chat.android.client.parser2.adapters.UpstreamReactionDtoAdapter
import io.getstream.chat.android.client.parser2.adapters.UpstreamUserDtoAdapter
import io.getstream.chat.android.client.parser2.adapters.UserResponsePrivacyFieldsAdapter
import io.getstream.chat.android.client.socket.ErrorResponse
import io.getstream.chat.android.client.socket.SocketErrorMessage
import io.getstream.chat.android.network.infrastructure.Serializer
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory

internal class MoshiChatParser(
    private val eventMapping: EventMapping,
    private val dtoMapping: DtoMapping,
    private val directEventParser: DirectEventParser?,
) : ChatParser {

    private val moshi: Moshi by lazy {
        Serializer.moshi.newBuilder()
            .addAdapter(ExactDateAdapter())
            .add(EventAdapterFactory())
            .add(DownstreamMessageDtoAdapter)
            .add(UpstreamMessageDtoAdapter)
            .add(DownstreamChannelDtoAdapter)
            .add(AttachmentDtoAdapter)
            .add(DownstreamReactionDtoAdapter)
            .add(UpstreamReactionDtoAdapter)
            .add(DownstreamUserDtoAdapter)
            .add(OwnUserResponseAdapter)
            .add(UserResponsePrivacyFieldsAdapter)
            .add(UpstreamUserDtoAdapter)
            .add(DownstreamMemberDtoAdapter)
            .add(UpstreamMemberDataDtoAdapter)
            .add(FlagRequestAdapterFactory)
            .add(DownstreamThreadDtoAdapter)
            .add(DownstreamThreadInfoDtoAdapter)
            .add(DownstreamPollDtoAdapter)
            .add(DownstreamPollOptionDtoAdapter)
            .add(CreatePollRequestAdapter)
            .add(UpstreamOptionDtoAdapter)
            .build()
    }

    private inline fun <reified T> Moshi.Builder.addAdapter(adapter: JsonAdapter<T>) = apply {
        this.add(T::class.java, adapter)
    }

    override fun configRetrofit(builder: Retrofit.Builder): Retrofit.Builder {
        return builder
            .addConverterFactory(MoshiUrlQueryPayloadFactory(moshi))
            .addConverterFactory(MoshiConverterFactory.create(moshi).withErrorLogging())
    }

    override fun toJson(any: Any): String = when {
        Map::class.java.isAssignableFrom(any.javaClass) -> serializeMap(any)
        any is ConnectedEvent -> serializeConnectedEvent(any)
        else -> moshi.adapter(any.javaClass).toJson(any)
    }

    private val mapAdapter = moshi.adapter(Map::class.java)

    private fun serializeMap(any: Any): String {
        return mapAdapter.toJson(any as Map<*, *>)
    }

    private val upstreamConnectedEventAdapter = moshi.adapter(UpstreamConnectedEventDto::class.java)

    private fun serializeConnectedEvent(connectedEvent: ConnectedEvent): String {
        val eventDto = with(dtoMapping) { connectedEvent.toDto() }
        return upstreamConnectedEventAdapter.toJson(eventDto)
    }

    @Suppress("UNCHECKED_CAST")
    override fun <T : Any> fromJson(raw: String, clazz: Class<T>): T {
        return when (clazz) {
            ChatEvent::class.java -> parseAndProcessEvent(raw) as T
            SocketErrorMessage::class.java -> parseSocketError(raw) as T
            ErrorResponse::class.java -> parseErrorResponse(raw) as T
            else -> return moshi.adapter(clazz).fromJson(raw)!!
        }
    }

    private val socketErrorResponseAdapter = moshi.adapter(SocketErrorResponse::class.java)

    private fun parseSocketError(raw: String): SocketErrorMessage {
        return socketErrorResponseAdapter.fromJson(raw)!!.toDomain()
    }

    private val errorResponseAdapter = moshi.adapter(SocketErrorResponse.ErrorResponse::class.java)

    private fun parseErrorResponse(raw: String): ErrorResponse {
        return errorResponseAdapter.fromJson(raw)!!.toDomain()
    }

    private val chatEventDtoAdapter = moshi.adapter(ChatEventDto::class.java)

    private val rawMapAdapter: JsonAdapter<MutableMap<String, Any?>> =
        moshi.adapter(Types.newParameterizedType(Map::class.java, String::class.java, Any::class.java))
    private val exactDateAdapter = moshi.adapter(ExactDate::class.java)
    private val downstreamUserDtoAdapter = moshi.adapter(DownstreamUserDto::class.java)

    private fun parseAndProcessEvent(raw: String): ChatEvent {
        directEventParser?.parse(raw)?.let { return it }

        val event = chatEventDtoAdapter.fromJson(raw)?.let { dto -> with(eventMapping) { dto.toDomain() } }
            ?: parseGeneratedEvent(raw)
            ?: buildUnknownEvent(raw)
        return event.enrichIfNeeded()
    }

    // Dispatcher for events that have been migrated off the hand-written ChatEventDto family.
    // Entries are added per-event during the events-DTO migration; returns null when the type
    // string isn't yet migrated so the caller falls through to the unknown-event path.
    private fun parseGeneratedEvent(raw: String): ChatEvent? = null

    private fun buildUnknownEvent(raw: String): ChatEvent {
        val map = rawMapAdapter.fromJson(raw)!!.filterValues { it != null }
        val dto = UnknownEventDto(
            type = (map["type"] as? String) ?: EventType.UNKNOWN,
            created_at = exactDateAdapter.fromJsonValue(map["created_at"])!!,
            user = downstreamUserDtoAdapter.fromJsonValue(map["user"]),
            rawData = map,
        )
        return with(eventMapping) { dto.toDomain() }
    }
}
