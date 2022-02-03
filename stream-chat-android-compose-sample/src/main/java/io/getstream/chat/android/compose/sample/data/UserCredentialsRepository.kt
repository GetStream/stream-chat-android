package io.getstream.chat.android.compose.sample.data

import android.content.Context
import android.content.SharedPreferences
import io.getstream.chat.android.client.models.User

/**
 * A repository that is used for persisting and fetching authentication data
 * for the currently logged-in user.
 */
class UserCredentialsRepository(context: Context) {

    private val prefs: SharedPreferences by lazy {
        context.getSharedPreferences(PREFS_USER_CREDENTIALS, Context.MODE_PRIVATE)
    }

    /**
     * Loads credentials for the logged-in user from persistent storage.
     */
    fun loadUserCredentials(): UserCredentials? {
        val apiKey = prefs.getString(KEY_API_KEY, null) ?: return null
        val userId = prefs.getString(KEY_USER_ID, null) ?: return null
        val userName = prefs.getString(KEY_USER_NAME, null) ?: return null
        val userImage = prefs.getString(KEY_USER_IMAGE, null) ?: return null
        val token = prefs.getString(KEY_USER_TOKEN, null) ?: return null

        return UserCredentials(
            apiKey = apiKey,
            user = User().apply {
                id = userId
                name = userName
                if (userImage.isNotEmpty()) {
                    image = userImage
                }
            },
            token = token
        )
    }

    /**
     * Loads an API key for the environment the user was logged into.
     */
    fun loadApiKey(): String? {
        return prefs.getString(KEY_API_KEY, null)
    }

    /**
     * Saves credentials for the logged-in user.
     */
    fun saveUserCredentials(userCredentials: UserCredentials) {
        prefs.edit()
            .putString(KEY_API_KEY, userCredentials.apiKey)
            .putString(KEY_USER_ID, userCredentials.user.id)
            .putString(KEY_USER_NAME, userCredentials.user.name)
            .putString(KEY_USER_IMAGE, userCredentials.user.image)
            .putString(KEY_USER_TOKEN, userCredentials.token)
            .apply()
    }

    /**
     * Removes user credentials from the persistent storage.
     */
    fun clearCredentials() {
        prefs.edit()
            .clear()
            .apply()
    }

    companion object {
        private const val PREFS_USER_CREDENTIALS = "user_credentials"

        private const val KEY_API_KEY = "api_key"
        private const val KEY_USER_ID = "user_id"
        private const val KEY_USER_NAME = "user_name"
        private const val KEY_USER_IMAGE = "user_image"
        private const val KEY_USER_TOKEN = "user_token"
    }
}
