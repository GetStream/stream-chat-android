/*
 * Copyright (c) 2014-2022 Stream.io Inc. All rights reserved.
 *
 * Licensed under the Stream License;
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    https://github.com/GetStream/stream-chat-android/blob/main/LICENSE
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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

    public companion object {
        private const val PREFS_NAME = "toggle_storage"

        private var instance: ToggleService? = null

        /**
         * Internal check used to avoid NPE in cases when SDK users don't initialize it.
         */
        internal fun isInitialized() = instance != null

        @InternalStreamChatApi
        public fun instance(): ToggleService = requireNotNull(instance) {
            "Toggle service must be initialized via the init method!"
        }

        @InternalStreamChatApi
        public fun init(appContext: Context, predefinedValues: Map<String, Boolean> = emptyMap()): ToggleService {
            val sp = appContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).also {
                predefinedValues.entries.forEach { (key, value) ->
                    if (it.contains(key).not()) {
                        it.edit().putBoolean(key, value).apply()
                    }
                }
            }

            return ToggleService(sp).also { instance = it }
        }

        @InternalStreamChatApi
        public fun isEnabled(featureKey: String): Boolean {
            return instance?.isEnabled(featureKey) ?: false
        }
    }
}
