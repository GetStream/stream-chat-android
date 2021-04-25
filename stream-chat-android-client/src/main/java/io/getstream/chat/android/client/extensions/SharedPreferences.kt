package io.getstream.chat.android.client.extensions

import android.content.SharedPreferences
import io.getstream.chat.android.core.internal.InternalStreamChatApi

/**
 * Unlike the getString method it delegates to, this method requires a non-null default value,
 * and therefore guarantees to return a non-null String.
 */
@InternalStreamChatApi
@Suppress("NOTHING_TO_INLINE")
public inline fun SharedPreferences.getNonNullString(key: String, defaultValue: String): String {
    return getString(key, defaultValue)!!
}
