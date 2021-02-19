package io.getstream.chat.android.livedata.extensions

import java.util.Date

internal fun Date?.greaterOrEqual(other: Date): Boolean = this?.after(other) == true || this == other

/**
 * Check if current date has difference with [other] no more that [offset].
 */
internal fun Date.inOffsetWith(other: Date, offset: Long): Boolean = (time + offset) > other.time
