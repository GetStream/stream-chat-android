package io.getstream.openapi.models
import com.squareup.moshi.JsonClass
import java.util.Date


@JsonClass(generateAdapter = true)
internal data class StreamChatChannelHiddenEvent(

    public val channel_id: String,

    public val channel_type: String,

    public val cid: String,

    public val clear_history: Boolean,

    public val created_at: Date,

    public val type: String,

    public val channel: StreamChatChannelResponse? = null,

    public val user: StreamChatUserObject? = null,

) : StreamChatWSEvent()