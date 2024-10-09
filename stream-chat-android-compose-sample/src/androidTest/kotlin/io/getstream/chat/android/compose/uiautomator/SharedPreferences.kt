/*
 * Copyright (c) 2014-2024 Stream.io Inc. All rights reserved.
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

package io.getstream.chat.android.compose.uiautomator

import android.content.SharedPreferences

inline fun SharedPreferences.save(valueKey: () -> Pair<Any, String>) {
    val (value, key) = valueKey()
    applyChanges {
        when (value) {
            is String -> putString(key, value)
            is Int -> putInt(key, value)
            is Float -> putFloat(key, value)
            is Boolean -> putBoolean(key, value)
        }
    }
}

inline fun SharedPreferences.remove(value: () -> String) {
    applyChanges {
        remove(value())
    }
}

inline fun SharedPreferences.applyChanges(executeAndApply: (SharedPreferences.Editor).() -> Unit) {
    val editor = edit()
    executeAndApply(editor)
    editor.commit()
}
