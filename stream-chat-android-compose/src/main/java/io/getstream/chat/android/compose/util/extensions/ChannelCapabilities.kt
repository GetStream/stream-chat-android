package io.getstream.chat.android.compose.util.extensions

import io.getstream.chat.android.client.models.ChannelCapabilities

/**
 * Creates a set of channel capabilities fully populated
 * with all values inside [ChannelCapabilities].
 *
 * Used for previews, using it in production would grant every user
 * all privileges.
 */
internal fun ChannelCapabilities.toSet(): Set<String> = setOf(
    BAN_CHANNEL_MEMBERS,
    CONNECT_EVENTS,
    DELETE_ANY_MESSAGE,
    DELETE_CHANNEL,
    DELETE_OWN_MESSAGE,
    FLAG_MESSAGE,
    FREEZE_CHANNEL,
    LEAVE_CHANNEL,
    MUTE_CHANNEL,
    PIN_MESSAGE,
    QUOTE_MESSAGE,
    READ_EVENTS,
    SEARCH_MESSAGES,
    SEND_CUSTOM_EVENTS,
    SEND_LINKS,
    SEND_MESSAGE,
    SEND_REACTION,
    SEND_REPLY,
    SET_CHANNEL_COOLDOWN,
    SEND_TYPING_EVENTS,
    UPDATE_ANY_MESSAGE,
    UPDATE_CHANNEL,
    UPDATE_CHANNEL_MEMBERS,
    UPDATE_OWN_MESSAGE,
    UPLOAD_FILE
)
