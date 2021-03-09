package io.getstream.chat.android.client.parser2

import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import io.getstream.chat.android.client.api2.MoshiUrlQueryPayloadFactory
import io.getstream.chat.android.client.api2.mapping.toDomain
import io.getstream.chat.android.client.api2.mapping.toDto
import io.getstream.chat.android.client.api2.model.dto.ChatEventDto
import io.getstream.chat.android.client.api2.model.dto.UpstreamConnectedEventDto
import io.getstream.chat.android.client.api2.model.response.SocketErrorResponse
import io.getstream.chat.android.client.events.ChatEvent
import io.getstream.chat.android.client.events.ConnectedEvent
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

    private fun serializeMap(any: Any): String {
        val adapter = moshi.adapter(Map::class.java)
        return adapter.toJson(any as Map<*, *>)
    }

    private fun serializeConnectedEvent(connectedEvent: ConnectedEvent): String {
        val eventDto = connectedEvent.toDto()
        val adapter = moshi.adapter(UpstreamConnectedEventDto::class.java)
        return adapter.toJson(eventDto)
    }

    override fun <T : Any> fromJson(raw: String, clazz: Class<T>): T {
        if (clazz == ChatEvent::class.java) {
            return parseAndMapEvent(raw)
        }
        if (clazz == SocketErrorMessage::class.java) {
            return parseSocketError(raw)
        }

        val adapter = moshi.adapter(clazz)
        return adapter.fromJson(raw)!!
    }

    @Suppress("UNCHECKED_CAST")
    private fun <T : Any> parseAndMapEvent(raw: String): T {
        val adapter = moshi.adapter(ChatEventDto::class.java)
        return adapter.fromJson(raw)!!.toDomain() as T
    }

    @Suppress("UNCHECKED_CAST")
    private fun <T : Any> parseSocketError(raw: String): T {
        val adapter = moshi.adapter(SocketErrorResponse::class.java)
        return adapter.fromJson(raw)!!.toDomain() as T
    }

    override fun configRetrofit(builder: Retrofit.Builder): Retrofit.Builder {
        return builder
            .addConverterFactory(MoshiUrlQueryPayloadFactory(moshi))
            .addConverterFactory(MoshiConverterFactory.create(moshi))
    }

    private inline fun <reified T> Moshi.Builder.addAdapter(adapter: JsonAdapter<T>) = apply {
        this.add(T::class.java, adapter)
    }
}
