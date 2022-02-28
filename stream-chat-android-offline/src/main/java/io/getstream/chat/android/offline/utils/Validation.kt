package io.getstream.chat.android.offline.utils

import io.getstream.chat.android.core.internal.InternalStreamChatApi
import kotlin.jvm.Throws

/**
 * Validates a cid. Verifies it's not empty and in the format messaging:123.
 *
 * @throws IllegalArgumentException If CID is invalid.
 */
@InternalStreamChatApi
@Throws(IllegalArgumentException::class)
internal fun validateCid(cid: String): String = cid.apply {
    require(cid.isNotEmpty()) { "cid can not be empty" }
    require(':' in cid) { "cid needs to be in the format channelType:channelId. For example, messaging:123" }
}
