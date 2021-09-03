package io.getstream.chat.android.client.utils.internal.toggle

import android.content.Context
import android.content.SharedPreferences
import io.getstream.chat.android.client.BuildConfig
import io.getstream.chat.android.core.internal.InternalStreamChatApi

@InternalStreamChatApi
public class ToggleService private constructor(private val sharedPreferences: SharedPreferences) {

    public fun isEnabled(featureKey: String): Boolean =
        sharedPreferences.getBoolean(featureKey, false) && BuildConfig.DEBUG

    public fun setToggle(featureKey: String, value: Boolean) {
        sharedPreferences.edit()
            .putBoolean(featureKey, value)
            .commit()
    }

    @Suppress("UNCHECKED_CAST")
    public fun getToggles(): Map<String, Boolean> =
        sharedPreferences.all.filter { it.value is Boolean }.toMap() as Map<String, Boolean>

    @InternalStreamChatApi
    public companion object {
        private const val PREFS_NAME = "toggle_storage"

        private var instance: ToggleService? = null

        @InternalStreamChatApi
        public fun instance(): ToggleService = requireNotNull(instance) {
            "Toggle service must be initialized via the init method!"
        }

        @InternalStreamChatApi
        public fun init(appContext: Context, predefinedValues: Map<String, Boolean> = emptyMap()): ToggleService {
            val sp = appContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).also {
                predefinedValues.entries.forEach { (key, value) ->
                    if (it.contains(key).not()) {
                        it.edit().putBoolean(key, value).commit()
                    }
                }
            }

            return ToggleService(sp).also { instance = it }
        }
    }
}
