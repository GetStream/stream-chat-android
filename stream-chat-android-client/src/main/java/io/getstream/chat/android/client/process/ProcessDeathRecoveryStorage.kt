/*
 * Copyright (c) 2014-2025 Stream.io Inc. All rights reserved.
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

package io.getstream.chat.android.client.process

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import io.getstream.chat.android.models.User

/**
 * Defines the reading/writing of the data required to recover the SDK state after the process death.
 */
internal interface ProcessDeathRecoveryStorage {

    /**
     * Reads the user from the storage.
     */
    fun readUser(): User?

    /**
     * Writes the user to the storage.
     */
    fun writeUser(user: User)

    /**
     * Clears the user from the storage.
     */
    fun clearUser()
}

/**
 * [ProcessDeathRecoveryStorage] implementation backed by [SharedPreferences].
 *
 * @param context The [Context] used to instantiate the [SharedPreferences].
 */
internal class SharedPreferencesProcessDeathRecoveryStorage(context: Context) : ProcessDeathRecoveryStorage {

    private val prefs: SharedPreferences =
        context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    override fun readUser(): User? {
        val id = prefs.getString(KEY_USER_ID, null) ?: return null
        val name = prefs.getString(KEY_USER_NAME, "") ?: ""
        val image = prefs.getString(KEY_USER_IMAGE, "") ?: ""
        val invisible = prefs.getBoolean(KEY_USER_INVISIBLE, false)
        return User(id = id, name = name, image = image, invisible = invisible)
    }

    override fun writeUser(user: User) {
        prefs.edit {
            putString(KEY_USER_ID, user.id)
            putString(KEY_USER_NAME, user.name)
            putString(KEY_USER_IMAGE, user.image)
            putBoolean(KEY_USER_INVISIBLE, user.invisible ?: false)
        }
    }

    override fun clearUser() {
        prefs.edit { clear() }
    }

    companion object {
        private const val PREFS_NAME = "stream_process_death_recovery_storage"

        private const val KEY_USER_ID = "user_id"
        private const val KEY_USER_NAME = "user_name"
        private const val KEY_USER_IMAGE = "user_image"
        private const val KEY_USER_INVISIBLE = "user_invisible"
    }
}
