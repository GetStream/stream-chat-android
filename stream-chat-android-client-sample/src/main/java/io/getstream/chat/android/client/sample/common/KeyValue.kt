package io.getstream.chat.android.client.sample.common

import android.content.Context

class KeyValue(context: Context) {

    private val prefs = context.getSharedPreferences("default-prefs", Context.MODE_PRIVATE)

    fun set(key: String, value: Boolean) {
        prefs.edit().putBoolean(key, value).apply()
    }

    fun getBoolean(key: String, default: Boolean): Boolean {
        return prefs.getBoolean(key, default)
    }

    companion object {
        const val CHANNELS_STORED = "channels-stored"
    }
}
