package io.getstream.chat.ui.sample.data.user

import android.content.Context
import android.content.SharedPreferences

class UserRepository(context: Context) {

    private val prefs: SharedPreferences by lazy {
        context.getSharedPreferences(USER_PREFS_NAME, Context.MODE_PRIVATE)
    }

    fun getUser(): SampleUser {
        val apiKey = prefs.getString(KEY_API_KEY, null)
        val id = prefs.getString(KEY_ID, null)
        val name = prefs.getString(KEY_NAME, null)
        val token = prefs.getString(KEY_TOKEN, null)
        val image = prefs.getString(KEY_IMAGE, null)
        return if (apiKey != null && id != null && name != null && token != null && image != null) {
            SampleUser(apiKey = apiKey, id = id, name = name, token = token, image = image)
        } else {
            SampleUser.None
        }
    }

    fun setUser(user: SampleUser) {
        prefs.edit()
            .putString(KEY_API_KEY, user.apiKey)
            .putString(KEY_ID, user.id)
            .putString(KEY_NAME, user.name)
            .putString(KEY_TOKEN, user.token)
            .putString(KEY_IMAGE, user.image)
            .apply()
    }

    fun clearUser() {
        prefs.edit().clear().apply()
    }

    private companion object {
        private const val USER_PREFS_NAME = "logged_in_user"
        private const val KEY_API_KEY = "api_key"
        private const val KEY_ID = "id"
        private const val KEY_NAME = "name"
        private const val KEY_TOKEN = "token"
        private const val KEY_IMAGE = "image"
    }
}
