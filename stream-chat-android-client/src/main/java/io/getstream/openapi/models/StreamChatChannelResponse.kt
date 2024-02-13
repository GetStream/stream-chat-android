package io.getstream.openapi.models
import com.squareup.moshi.JsonClass
import io.getstream.chat.android.client.parser2.adapters.RawJson

import java.util.Date


@JsonClass(generateAdapter = true)
internal data class StreamChatChannelResponse(

    public val cid: String,

    public val created_at: Date,

    public val disabled: Boolean,

    public val frozen: Boolean,

    public val id: String,

    public val type: String,

    public val updated_at: Date,

    public val custom: RawJson,

    public val auto_translation_enabled: Boolean? = null,

    public val auto_translation_language: String? = null,

    public val cooldown: Int? = null,

    public val deleted_at: Date? = null,

    public val hidden: Boolean? = null,

    public val hide_messages_before: Date? = null,

    public val last_message_at: Date? = null,

    public val member_count: Int? = null,

    public val mute_expires_at: Date? = null,

    public val muted: Boolean? = null,

    public val team: String? = null,

    public val truncated_at: Date? = null,

    public val members: List<StreamChatChannelMember?>? = null,

    public val own_capabilities: List<String>? = null,

    public val config: StreamChatChannelConfigWithInfo? = null,

    public val created_by: StreamChatUserObject? = null,

    public val truncated_by: StreamChatUserObject? = null,

) 