package io.getstream.openapi.models
import com.squareup.moshi.JsonClass
import java.util.Date


@JsonClass(generateAdapter = true)
internal data class StreamChatUserPresenceChangedEvent(

    public val created_at: Date,

    public val type: String,

    public val user: StreamChatUserObject? = null,

) : StreamChatWSEvent()