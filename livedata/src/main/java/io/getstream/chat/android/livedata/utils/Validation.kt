package io.getstream.chat.android.livedata.utils

import java.security.InvalidParameterException

/**
 * Validates a cid. Verifies it's not empty and in the format messaging:123
 */
internal fun validateCid(cid: String) {
    if (cid.isEmpty()) {
        throw InvalidParameterException("cid cant be empty")
    }
    if (!cid.contains(":")) {
        throw InvalidParameterException("cid needs to be in the format channelType:channelId. For example messaging:123")
    }
}
