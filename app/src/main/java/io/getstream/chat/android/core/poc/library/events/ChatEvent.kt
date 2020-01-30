package io.getstream.chat.android.core.poc.library.events

import com.google.gson.annotations.SerializedName
import io.getstream.chat.android.core.poc.library.EventType
import io.getstream.chat.android.core.poc.library.utils.UndefinedDate
import java.util.*


open class ChatEvent {

    lateinit var type: String

    @SerializedName("created_at")
    val createdAt: Date = UndefinedDate

    var receivedAt: Date = Date()

    fun getType(): EventType {
        return EventType.values().firstOrNull {
            it.label == type
        } ?: EventType.UNKNOWN
    }
}