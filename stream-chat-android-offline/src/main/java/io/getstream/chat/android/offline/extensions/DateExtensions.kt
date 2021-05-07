package io.getstream.chat.android.offline.extensions

import java.util.Date

/**
 * Check if current date has difference with [other] no more that [offset].
 */
internal fun Date.inOffsetWith(other: Date, offset: Long): Boolean = (time + offset) >= other.time
