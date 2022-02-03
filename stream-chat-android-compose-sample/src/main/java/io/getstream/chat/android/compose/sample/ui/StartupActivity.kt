package io.getstream.chat.android.compose.sample.ui

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.TaskStackBuilder
import io.getstream.chat.android.compose.sample.ChatApp
import io.getstream.chat.android.compose.sample.ChatHelper
import io.getstream.chat.android.compose.sample.ui.login.UserLoginActivity

/**
 * An Activity without UI responsible for startup routing. It navigates the user to
 * one of the following screens:
 *
 * - Login screen, if the user is not authenticated
 * - Channels screen, if the user is authenticated
 * - Messages screen, if the user is coming from a push notification
 */
class StartupActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val userCredentials = ChatApp.credentialsRepository.loadUserCredentials()
        if (userCredentials != null) {
            // Ensure that the user is connected
            ChatHelper.connectUser(userCredentials)

            if (intent.hasExtra(KEY_CHANNEL_ID)) {
                // Navigating from push, route to the messages screen
                val channelId = requireNotNull(intent.getStringExtra(KEY_CHANNEL_ID))
                TaskStackBuilder.create(this)
                    .addNextIntent(ChannelsActivity.createIntent(this))
                    .addNextIntent(MessagesActivity.createIntent(this, channelId))
                    .startActivities()
            } else {
                // Logged in, navigate to the channels screen
                startActivity(ChannelsActivity.createIntent(this))
            }
        } else {
            // Not logged in, start with the login screen
            startActivity(UserLoginActivity.createIntent(this))
        }
        finish()
    }

    companion object {
        private const val KEY_CHANNEL_ID = "channelId"

        fun createIntent(context: Context, channelId: String): Intent {
            return Intent(context, StartupActivity::class.java).apply {
                putExtra(KEY_CHANNEL_ID, channelId)
            }
        }
    }
}
