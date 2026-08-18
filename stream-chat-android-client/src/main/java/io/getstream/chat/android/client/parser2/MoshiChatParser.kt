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
import io.getstream.chat.android.client.api2.FlagRequestAdapterFactory
import io.getstream.chat.android.client.api2.MoshiUrlQueryPayloadFactory
import io.getstream.chat.android.client.api2.mapping.DtoMapping
import io.getstream.chat.android.client.api2.mapping.EventMapping
import io.getstream.chat.android.client.api2.mapping.toDomain
import io.getstream.chat.android.client.api2.model.dto.ChatEventDto
import io.getstream.chat.android.client.api2.model.dto.UpstreamConnectedEventDto
import io.getstream.chat.android.client.api2.model.response.SocketErrorResponse
import io.getstream.chat.android.client.events.ChatEvent
import io.getstream.chat.android.client.events.ConnectedEvent
import io.getstream.chat.android.client.extensions.internal.enrichIfNeeded
import io.getstream.chat.android.client.parser.ChatParser
import io.getstream.chat.android.client.parser2.adapters.AttachmentDtoAdapter
import io.getstream.chat.android.client.parser2.adapters.AttachmentRequestAdapter
import io.getstream.chat.android.client.parser2.adapters.ChannelInputRequestAdapter
import io.getstream.chat.android.client.parser2.adapters.ChannelMemberRequestAdapter
import io.getstream.chat.android.client.parser2.adapters.CreatePollOptionRequestAdapter
import io.getstream.chat.android.client.parser2.adapters.CreatePollRequestAdapter
import io.getstream.chat.android.client.parser2.adapters.DownstreamChannelDtoAdapter
import io.getstream.chat.android.client.parser2.adapters.DownstreamMemberDtoAdapter
import io.getstream.chat.android.client.parser2.adapters.DownstreamMemberInfoDtoAdapter
import io.getstream.chat.android.client.parser2.adapters.DownstreamMessageDtoAdapter
import io.getstream.chat.android.client.parser2.adapters.DownstreamModerationDetailsDtoAdapter
import io.getstream.chat.android.client.parser2.adapters.DownstreamPollDtoAdapter
import io.getstream.chat.android.client.parser2.adapters.DownstreamPollOptionDtoAdapter
import io.getstream.chat.android.client.parser2.adapters.DownstreamReactionDtoAdapter
import io.getstream.chat.android.client.parser2.adapters.DownstreamThreadDtoAdapter
import io.getstream.chat.android.client.parser2.adapters.DownstreamThreadInfoDtoAdapter
import io.getstream.chat.android.client.parser2.adapters.DownstreamUserDtoAdapter
import io.getstream.chat.android.client.parser2.adapters.EventAdapterFactory
import io.getstream.chat.android.client.parser2.adapters.EventRequestAdapter
import io.getstream.chat.android.client.parser2.adapters.ExactDateAdapter
import io.getstream.chat.android.client.parser2.adapters.MessageRequestAdapter
import io.getstream.chat.android.client.parser2.adapters.NullCollectionsAsEmptyFactory
import io.getstream.chat.android.client.parser2.adapters.PollOptionInputAdapter
import io.getstream.chat.android.client.parser2.adapters.PollOptionRequestAdapter
import io.getstream.chat.android.client.parser2.adapters.PollOptionResponseDataAdapter
import io.getstream.chat.android.client.parser2.adapters.UpdatePollOptionRequestAdapter
import io.getstream.chat.android.client.parser2.adapters.UpdatePollRequestAdapter
import io.getstream.chat.android.client.parser2.adapters.UpstreamMemberDataDtoAdapter
import io.getstream.chat.android.client.parser2.adapters.UpstreamMemberDtoAdapter
import io.getstream.chat.android.client.parser2.adapters.UpstreamMessageDtoAdapter
import io.getstream.chat.android.client.parser2.adapters.UpstreamReactionDtoAdapter
import io.getstream.chat.android.client.parser2.adapters.UpstreamUserDtoAdapter
import io.getstream.chat.android.client.parser2.adapters.UserRequestAdapter
import io.getstream.chat.android.client.parser2.adapters.UserResponseAdapter
import io.getstream.chat.android.client.socket.ErrorResponse
import io.getstream.chat.android.client.socket.SocketErrorMessage
import io.getstream.chat.android.network.infrastructure.Serializer
import io.getstream.chat.android.network.models.ConfigOverridesRequest
import io.getstream.chat.android.network.models.CreatePollRequest
import io.getstream.chat.android.network.models.MessageRequest
import io.getstream.chat.android.network.models.TranslateMessageRequest
import io.getstream.chat.android.network.models.UpdatePollRequest
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
            .add(DownstreamModerationDetailsDtoAdapter)
            .add(UpstreamMessageDtoAdapter)
            .add(DownstreamChannelDtoAdapter)
            .add(AttachmentDtoAdapter)
            .add(DownstreamReactionDtoAdapter)
            .add(UpstreamReactionDtoAdapter)
            .add(DownstreamUserDtoAdapter)
            .add(UpstreamUserDtoAdapter)
            .add(UserResponseAdapter)
            .add(UserRequestAdapter)
            .add(AttachmentRequestAdapter)
            .add(MessageRequestAdapter)
            .add(ChannelMemberRequestAdapter)
            .add(ChannelInputRequestAdapter)
            .add(DownstreamMemberDtoAdapter)
            .add(DownstreamMemberInfoDtoAdapter)
            .add(UpstreamMemberDtoAdapter)
            .add(UpstreamMemberDataDtoAdapter)
            .add(FlagRequestAdapterFactory)
            .add(DownstreamThreadDtoAdapter)
            .add(DownstreamThreadInfoDtoAdapter)
            .add(DownstreamPollDtoAdapter)
            .add(DownstreamPollOptionDtoAdapter)
            .add(CreatePollRequestAdapter)
            .add(UpdatePollRequestAdapter)
            .add(CreatePollOptionRequestAdapter)
            .add(UpdatePollOptionRequestAdapter)
            .add(PollOptionInputAdapter)
            .add(PollOptionRequestAdapter)
            .add(EventRequestAdapter)
            .add(PollOptionResponseDataAdapter)
            .add(
                CreatePollRequest.VotingVisibility::class.java,
                CreatePollRequest.VotingVisibility.VotingVisibilityAdapter(),
            )
            .add(
                UpdatePollRequest.VotingVisibility::class.java,
                UpdatePollRequest.VotingVisibility.VotingVisibilityAdapter(),
            )
            .add(
                MessageRequest.Type::class.java,
                MessageRequest.Type.TypeAdapter(),
            )
            .add(
                ConfigOverridesRequest.BlocklistBehavior::class.java,
                ConfigOverridesRequest.BlocklistBehavior.BlocklistBehaviorAdapter(),
            )
            .add(
                ConfigOverridesRequest.PushLevel::class.java,
                ConfigOverridesRequest.PushLevel.PushLevelAdapter(),
            )
            .add(
                TranslateMessageRequest.Language::class.java,
                TranslateMessageRequest.Language.LanguageAdapter(),
            )
            // Registered last so the model-specific adapters above keep precedence and delegate into it.
            .add(NullCollectionsAsEmptyFactory)
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

    private fun parseAndProcessEvent(raw: String): ChatEvent {
        val directEvent = directEventParser?.parse(raw)
        if (directEvent != null) {
            // Direct adapters handle enrichment inline — no enrichIfNeeded() needed.
            return directEvent
        }
        return with(eventMapping) { chatEventDtoAdapter.fromJson(raw)!!.toDomain() }.enrichIfNeeded()
    }
}
