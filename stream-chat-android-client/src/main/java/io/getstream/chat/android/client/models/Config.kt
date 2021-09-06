package io.getstream.chat.android.client.models

import java.util.Date

public data class Config(

    /**
     * Date of channel creation.
     */
    val createdAt: Date? = null,

    /**
     * Date of last channel update.
     */
    val updatedAt: Date? = null,

    /**
     * The name of the channel type must be unique per application
     */
    val name: String = "",

    /**
     * Controls if typing indicators are shown. Enabled by default.
     */
    val typingEventsEnabled: Boolean = true,

    /**
     * Controls whether the chat shows how far you’ve read. Enabled by default.
     */
    val readEventsEnabled: Boolean = true,

    /**
     * Determines if events are fired for connecting and disconnecting to a chat. Enabled by default.
     */
    val connectEventsEnabled: Boolean = true,

    /**
     * Controls if messages should be searchable (this is a premium feature). Disabled by default.
     */
    val searchEnabled: Boolean = true,

    /**
     * If users are allowed to add reactions to messages. Enabled by default.
     */
    val isReactionsEnabled: Boolean = true,

    /**
     * Enables message threads and replies. Enabled by default.
     */
    val isRepliesEnabled: Boolean = true,

    /**
     * Determines if users are able to mute other users. Enabled by default.
     */
    val muteEnabled: Boolean = true,

    /**
     * Allows image and file uploads within messages. Enabled by default.
     */
    val uploadsEnabled: Boolean = true,

    /**
     * Determines if URL enrichment enabled to show they as attachments. Enabled by default.
     */
    val urlEnrichmentEnabled: Boolean = true,

    val customEventsEnabled: Boolean = false,

    val pushNotificationsEnabled: Boolean = true,

    /**
     * A number of days or infinite. "Infinite" by default.
     */
    val messageRetention: String = "infinite",

    /**
     * The max message length. 5000 by default.
     */
    val maxMessageLength: Int = 5000,

    /**
     * Disabled, simple or AI are valid options for the Automod. AI based moderation is a premium feature.
     */
    val automod: String = "disabled",

    val automodBehavior: String = "",

    val blocklistBehavior: String = "",

    /**
     * The commands that are available on this channel type, e.g. /giphy.
     */
    val commands: List<Command> = mutableListOf(),
)
