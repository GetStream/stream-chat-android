package io.getstream.chat.android.client.parser2

import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import io.getstream.chat.android.client.api2.MoshiUrlQueryPayloadFactory
import io.getstream.chat.android.client.api2.mapping.toDomain
import io.getstream.chat.android.client.api2.mapping.toDto
import io.getstream.chat.android.client.api2.model.dto.ChatEventDto
import io.getstream.chat.android.client.api2.model.dto.UpstreamConnectedEventDto
import io.getstream.chat.android.client.api2.model.response.SocketErrorResponse
import io.getstream.chat.android.client.events.ChannelCreatedEvent
import io.getstream.chat.android.client.events.ChannelUpdatedByUserEvent
import io.getstream.chat.android.client.events.ChannelUpdatedEvent
import io.getstream.chat.android.client.events.ChatEvent
import io.getstream.chat.android.client.events.ConnectedEvent
import io.getstream.chat.android.client.events.MessageDeletedEvent
import io.getstream.chat.android.client.events.MessageUpdatedEvent
import io.getstream.chat.android.client.events.NewMessageEvent
import io.getstream.chat.android.client.events.NotificationMessageNewEvent
import io.getstream.chat.android.client.events.ReactionDeletedEvent
import io.getstream.chat.android.client.events.ReactionNewEvent
import io.getstream.chat.android.client.events.ReactionUpdateEvent
import io.getstream.chat.android.client.extensions.enrichWithCid
import io.getstream.chat.android.client.parser.ChatParser
import io.getstream.chat.android.client.parser2.adapters.AttachmentDtoAdapter
import io.getstream.chat.android.client.parser2.adapters.DateAdapter
import io.getstream.chat.android.client.parser2.adapters.DownstreamChannelDtoAdapter
import io.getstream.chat.android.client.parser2.adapters.DownstreamMessageDtoAdapter
import io.getstream.chat.android.client.parser2.adapters.DownstreamReactionDtoAdapter
import io.getstream.chat.android.client.parser2.adapters.DownstreamUserDtoAdapter
import io.getstream.chat.android.client.parser2.adapters.EventAdapterFactory
import io.getstream.chat.android.client.parser2.adapters.UpstreamChannelDtoAdapter
import io.getstream.chat.android.client.parser2.adapters.UpstreamMessageDtoAdapter
import io.getstream.chat.android.client.parser2.adapters.UpstreamReactionDtoAdapter
import io.getstream.chat.android.client.parser2.adapters.UpstreamUserDtoAdapter
import io.getstream.chat.android.client.socket.SocketErrorMessage
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory

internal class MoshiChatParser : ChatParser {

    private val moshi: Moshi by lazy {
        Moshi.Builder()
            .addAdapter(DateAdapter())
            .add(EventAdapterFactory())
            .add(DownstreamMessageDtoAdapter)
            .add(UpstreamMessageDtoAdapter)
            .add(DownstreamChannelDtoAdapter)
            .add(UpstreamChannelDtoAdapter)
            .add(AttachmentDtoAdapter)
            .add(DownstreamReactionDtoAdapter)
            .add(UpstreamReactionDtoAdapter)
            .add(DownstreamUserDtoAdapter)
            .add(UpstreamUserDtoAdapter)
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

    override fun toJson(any: Any): String {
        if (Map::class.java.isAssignableFrom(any.javaClass)) {
            return serializeMap(any)
        }
        if (any is ConnectedEvent) {
            return serializeConnectedEvent(any)
        }

        val adapter = moshi.adapter(any.javaClass)
        return adapter.toJson(any)
    }

    private val mapAdapter = moshi.adapter(Map::class.java)

    private fun serializeMap(any: Any): String {
        return mapAdapter.toJson(any as Map<*, *>)
    }

    private val upstreamConnectedEventAdapter = moshi.adapter(UpstreamConnectedEventDto::class.java)

    private fun serializeConnectedEvent(connectedEvent: ConnectedEvent): String {
        val eventDto = connectedEvent.toDto()
        return upstreamConnectedEventAdapter.toJson(eventDto)
    }

    override fun <T : Any> fromJson(raw: String, clazz: Class<T>): T {
        if (clazz == ChatEvent::class.java) {
            @Suppress("UNCHECKED_CAST")
            return parseAndProcessEvent(raw) as T
        }
        if (clazz == SocketErrorMessage::class.java) {
            @Suppress("UNCHECKED_CAST")
            return parseSocketError(raw) as T
        }

        val adapter = moshi.adapter(clazz)
        return adapter.fromJson(raw)!!
    }

    private val socketErrorResponseAdapter = moshi.adapter(SocketErrorResponse::class.java)

    @Suppress("UNCHECKED_CAST")
    private fun parseSocketError(raw: String): SocketErrorMessage {
        return socketErrorResponseAdapter.fromJson(raw)!!.toDomain()
    }

    private val chatEventDtoAdapter = moshi.adapter(ChatEventDto::class.java)

    @Suppress("UNCHECKED_CAST")
    private fun parseAndProcessEvent(raw: String): ChatEvent {
        val event = chatEventDtoAdapter.fromJson(raw)!!.toDomain()
        return event.enrichIfNeeded()
    }

    private fun ChatEvent.enrichIfNeeded(): ChatEvent = apply {
        when (this) {
            is NewMessageEvent -> message.enrichWithCid(cid)
            is MessageDeletedEvent -> message.enrichWithCid(cid)
            is MessageUpdatedEvent -> message.enrichWithCid(cid)
            is ReactionNewEvent -> message.enrichWithCid(cid)
            is ReactionUpdateEvent -> message.enrichWithCid(cid)
            is ReactionDeletedEvent -> message.enrichWithCid(cid)
            is ChannelCreatedEvent -> message?.enrichWithCid(cid)
            is ChannelUpdatedEvent -> message?.enrichWithCid(cid)
            is ChannelUpdatedByUserEvent -> message?.enrichWithCid(cid)
            is NotificationMessageNewEvent -> message.enrichWithCid(cid)
            else -> { /* Do nothing */
            }
        }
    }
}
