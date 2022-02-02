package io.getstream.chat.android.offline.utils

import io.getstream.chat.android.core.internal.InternalStreamChatApi

/**
 * Converts a pair of channelType and channelId into cid.
 *
 * @return String CID of the given channel type and id.
 */
@Throws(IllegalStateException::class)
@InternalStreamChatApi
public fun Pair<String, String>.toCid(): String {
    val cid = checkNotNull("$first:$second")
    validateCid(cid)
    return cid
}
