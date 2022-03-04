package io.getstream.chat.android.client.models

import io.getstream.chat.android.core.internal.InternalStreamChatApi

/**
 * A [Channel] object that contains less information.
 * Used only internally.
 *
 * @param cid The channel id in the format messaging:123.
 * @param id Channel's unique ID.
 * @param type Type of the channel.
 * @param memberCount Number of members in the channel.
 * @param name Channel's name.
 * @param image Channel's image.
 */
@InternalStreamChatApi
public data class ChannelInfo(
    val cid: String? = null,
    val id: String? = null,
    val type: String? = null,
    val memberCount: Int = 0,
    val name: String? = null,
    val image: String? = null,
)
