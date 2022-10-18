package io.getstream.chat.android.offline.repository.realm.utils

import io.realm.kotlin.types.RealmInstant
import java.util.Date

private const val ONE_K: Int = 1000

internal fun RealmInstant.toDate() = Date(this.epochSeconds * ONE_K)

internal fun Date.toRealmInstant() = RealmInstant.from(time / ONE_K, 0)
