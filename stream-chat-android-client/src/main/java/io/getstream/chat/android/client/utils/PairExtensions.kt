package io.getstream.chat.android.client.utils

import io.getstream.chat.android.core.internal.InternalStreamChatApi

/**
 * Converts a pair of channelType and channelId into cid.
 *
 * @return String CID of the given channel type and id.
 * @throws IllegalArgumentException if cid is not valid.
 */
@Throws(IllegalArgumentException::class)
@InternalStreamChatApi
public fun Pair<String, String>.toCid(): String {
    val cid = "$first:$second"
    validateCid(cid)
    return cid
}
