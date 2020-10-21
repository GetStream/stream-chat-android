package io.getstream.chat.android.client.parser

import com.google.gson.ExclusionStrategy
import com.google.gson.FieldAttributes
import com.google.gson.GsonBuilder
import com.google.gson.TypeAdapter
import io.getstream.chat.android.client.events.ChatEvent
import org.amshove.kluent.`should be equal to`
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource

internal class EventAdapterTest {

    private val gson = GsonBuilder()
        .registerTypeAdapterFactory(TypeAdapterFactory())
        .addSerializationExclusionStrategy(
            object : ExclusionStrategy {
                override fun shouldSkipClass(clazz: Class<*>): Boolean = false
                override fun shouldSkipField(f: FieldAttributes): Boolean =
                    f.getAnnotation(IgnoreSerialisation::class.java) != null
            }
        )
        .addDeserializationExclusionStrategy(
            object : ExclusionStrategy {
                override fun shouldSkipClass(clazz: Class<*>): Boolean = false
                override fun shouldSkipField(f: FieldAttributes): Boolean =
                    f.getAnnotation(IgnoreDeserialisation::class.java) != null
            }
        )
        .create()

    private val eventAdapter: TypeAdapter<ChatEvent> =
        EventAdapter(gson, gson.getAdapter(ChatEvent::class.java))

    @ParameterizedTest
    @MethodSource("io.getstream.chat.android.client.parser.EventArguments#eventAdapterArguments")
    fun `Should create proper event`(eventData: String, expectedEvent: ChatEvent) {
        eventAdapter.fromJson(eventData) `should be equal to` expectedEvent
    }
}
