package io.getstream.chat.android.livedata.utils

import android.content.SharedPreferences

/**
 * Unlike the getString method it delegates to, this method requires a non-null default value,
 * and therefore guarantees to return a non-null String.
 */
@Suppress("NOTHING_TO_INLINE")
internal inline fun SharedPreferences.getNonNullString(key: String, defaultValue: String): String {
    return getString(key, defaultValue)!!
}
