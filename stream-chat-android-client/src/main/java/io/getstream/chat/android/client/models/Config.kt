package io.getstream.chat.android.client.models

import com.google.gson.annotations.SerializedName
import java.util.Date

public data class Config(

    /**
     * Date of channel creation.
     */
    @SerializedName("created_at")
    val created_at: Date? = null,

    /**
     * Date of last channel update.
     */
    @SerializedName("updated_at")
    val updated_at: Date? = null,

    /**
     * The name of the channel type must be unique per application
     */
    val name: String = "",

    /**
     * Controls if typing indicators are shown. Enabled by default.
     */
    @SerializedName("typing_events")
    var isTypingEvents: Boolean = false,

    /**
     * Controls whether the chat shows how far you’ve read. Enabled by default.
     */
    @SerializedName("read_events")
    var isReadEvents: Boolean = false,

    /**
     * Determines if events are fired for connecting and disconnecting to a chat. Enabled by default.
     */
    @SerializedName("connect_events")
    val isConnectEvents: Boolean = false,

    /**
     * Controls if messages should be searchable (this is a premium feature). Disabled by default.
     */
    @SerializedName("search")
    val isSearch: Boolean = false,

    /**
     * If users are allowed to add reactions to messages. Enabled by default.
     */
    @SerializedName("reactions")
    val isReactionsEnabled: Boolean = false,

    /**
     * Enables message threads and replies. Enabled by default.
     */
    @SerializedName("replies")
    val isRepliesEnabled: Boolean = false,

    /**
     * Determines if users are able to mute other users. Enabled by default.
     */
    @SerializedName("mutes")
    val isMutes: Boolean = false,

    /**
     * Allows image and file uploads within messages. Enabled by default.
     */
    @SerializedName("uploads")
    val isUploads: Boolean = false,

    /**
     * Determines if URL enrichment enabled to show they as attachments. Enabled by default.
     */
    @SerializedName("url_enrichment")
    val isUrlEnrichment: Boolean = false,

    @SerializedName("custom_events")
    val isCustomEvents: Boolean = false,

    @SerializedName("push_notifications")
    val isPushNotifications: Boolean = false,

    /**
     * A number of days or infinite. "Infinite" by default.
     */
    @SerializedName("message_retention")
    val messageRetention: String = "",

    /**
     * The max message length. 5000 by default.
     */
    @SerializedName("max_message_length")
    val maxMessageLength: Int = Int.MAX_VALUE,

    /**
     * Disabled, simple or AI are valid options for the Automod. AI based moderation is a premium feature.
     */
    val automod: String = "",

    @SerializedName("automod_behavior")
    val automodBehavior: String = "",

    @SerializedName("blocklist_behavior")
    val blocklistBehavior: String = "",

    /**
     * The commands that are available on this channel type, e.g. /giphy.
     */
    val commands: List<Command> = mutableListOf(),
)
