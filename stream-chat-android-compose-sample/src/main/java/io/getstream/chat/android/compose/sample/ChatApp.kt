package io.getstream.chat.android.compose.sample

import android.app.Application
import com.getstream.sdk.chat.utils.DateFormatter
import io.getstream.chat.android.compose.sample.data.PredefinedUserCredentials
import io.getstream.chat.android.compose.sample.data.UserCredentialsRepository

class ChatApp : Application() {

    override fun onCreate() {
        super.onCreate()
        // Done for simplicity, use a DI framework instead
        chatManager = ChatManager(this)
        credentialsRepository = UserCredentialsRepository(this)
        dateFormatter = DateFormatter.from(this)

        // Initialize Stream SDK
        chatManager.initializeSdk(getApiKey())
    }

    private fun getApiKey(): String {
        return credentialsRepository.loadApiKey() ?: PredefinedUserCredentials.API_KEY
    }

    companion object {
        lateinit var chatManager: ChatManager
            private set

        lateinit var credentialsRepository: UserCredentialsRepository
            private set

        lateinit var dateFormatter: DateFormatter
            private set
    }
}
