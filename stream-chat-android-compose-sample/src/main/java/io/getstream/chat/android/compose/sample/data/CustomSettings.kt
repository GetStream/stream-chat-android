/*
 * Copyright (c) 2014-2026 Stream.io Inc. All rights reserved.
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

package io.getstream.chat.android.compose.sample.data

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

/**
 * A class that encapsulates custom settings for the app.
 */
class CustomSettings(private val context: Context) {

    private val prefs: SharedPreferences by lazy {
        context.getSharedPreferences("custom_settings", Context.MODE_PRIVATE)
    }

    var isAdaptiveLayoutEnabled: Boolean by booleanPref(AdaptiveLayout)

    var isComposerFloatingStyleEnabled: Boolean by booleanPref(ComposerFloatingStyle)

    private fun booleanPref(key: String, default: Boolean = false) =
        object : ReadWriteProperty<Any?, Boolean> {
            override fun getValue(thisRef: Any?, property: KProperty<*>) =
                prefs.getBoolean(key, default)

            override fun setValue(thisRef: Any?, property: KProperty<*>, value: Boolean) =
                prefs.edit { putBoolean(key, value) }
        }
}

private const val AdaptiveLayout = "adaptive_layout"
private const val ComposerFloatingStyle = "composer_floating_style"

fun Context.customSettings(): CustomSettings = CustomSettings(this)
