package io.getstream.chat.android.offline.utils

import io.getstream.chat.android.client.utils.Result

/**
 * Validates a cid. Verifies it's not empty and in the format messaging:123.
 *
 * @param cid The full channel id. ie messaging:123.
 *
 * @throws IllegalArgumentException If CID is invalid.
 */
@Throws(IllegalArgumentException::class)
internal fun validateCid(cid: String): String = cid.apply {
    require(cid.isNotEmpty()) { "cid can not be empty" }
    require(':' in cid) { "cid needs to be in the format channelType:channelId. For example, messaging:123" }
}

/**
 * Safely validates a cid and returns a result.
 *
 * @param cid The full channel id. ie messaging:123.
 *
 * @return Successful [Result] if the cid is valid.
 */
@Suppress("UNCHECKED_CAST")
internal fun <T : Any> validateCidWithResult(cid: String): Result<T> {
    return try {
        validateCid(cid)
        Result.success(Unit as T)
    } catch (exception: IllegalArgumentException) {
        Result.error(exception)
    }
}
