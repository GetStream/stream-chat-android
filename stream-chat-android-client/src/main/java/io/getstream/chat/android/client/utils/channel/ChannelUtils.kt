package io.getstream.chat.android.client.utils.channel

import io.getstream.chat.android.core.internal.InternalStreamChatApi

/**
 * Generates the channel id based on the member ids if provided [channelId] is empty.
 * Member-based id should only be used for creating distinct channels.
 * Created member-based id might differ from the one created by the server if the channel already exists
 * and was created with different members order.
 *
 * @param channelId The channel id. ie 123.
 * @param memberIds The list of members' ids.
 *
 * @return [channelId] if not blank or new, member-based id.
 */
@InternalStreamChatApi
public fun generateChannelIdIfNeeded(channelId: String, memberIds: List<String>): String {
    return channelId.ifBlank {
        memberIds.joinToString(prefix = "!members-")
    }
}
