package io.getstream.chat.android.livedata.extensions

import io.getstream.chat.android.client.models.Message
import java.util.Date

internal fun Message.wasCreatedAfterOrAt(date: Date?): Boolean = createdAt ?: createdLocallyAt ?: NEVER >= date
internal fun Message.wasCreatedAfter(date: Date?): Boolean = createdAt ?: createdLocallyAt ?: NEVER > date
internal fun Message.wasCreatedBefore(date: Date?): Boolean = createdAt ?: createdLocallyAt ?: NEVER < date
internal fun Message.wasCreatedBeforeOrAt(date: Date?): Boolean = createdAt ?: createdLocallyAt ?: NEVER <= date
internal val NEVER = Date(0)
