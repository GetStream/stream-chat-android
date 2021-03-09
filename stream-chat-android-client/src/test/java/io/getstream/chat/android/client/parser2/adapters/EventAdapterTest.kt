package io.getstream.chat.android.client.parser2.adapters

import com.squareup.moshi.Moshi
import io.getstream.chat.android.client.api2.mapping.toDomain
import io.getstream.chat.android.client.api2.model.dto.ChatEventDto
import io.getstream.chat.android.client.events.ChatEvent
import org.amshove.kluent.`should be equal to`
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import java.util.Date

internal class EventAdapterTest {

    private val moshi = Moshi.Builder()
        .add(Date::class.java, DateAdapter())
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

    private val eventAdapter = moshi.adapter(ChatEventDto::class.java)

    /** [io.getstream.chat.android.client.parser.EventArguments.eventAdapterArguments] */
    @ParameterizedTest
    @MethodSource("io.getstream.chat.android.client.parser.EventArguments#eventAdapterArguments")
    fun `Should create proper event`(eventData: String, expectedEvent: ChatEvent) {
        eventAdapter.fromJson(eventData)?.toDomain() `should be equal to` expectedEvent
    }
}
