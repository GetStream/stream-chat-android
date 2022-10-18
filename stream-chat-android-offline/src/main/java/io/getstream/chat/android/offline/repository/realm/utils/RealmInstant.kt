package io.getstream.chat.android.offline.repository.realm.utils

import io.realm.kotlin.types.RealmInstant
import java.util.Date

internal fun RealmInstant.toDate() = Date(this.epochSeconds * 1000)

internal fun Date.toRealmInstant() = RealmInstant.from(time/1000, 0)
