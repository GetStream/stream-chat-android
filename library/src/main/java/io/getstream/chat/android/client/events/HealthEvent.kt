package io.getstream.chat.android.client.events

import com.google.gson.annotations.SerializedName
import io.getstream.chat.android.client.models.EventType

data class HealthEvent(
    @SerializedName("connection_id")
    val connectionId: String
) : ChatEvent(EventType.HEALTH_CHECK)
