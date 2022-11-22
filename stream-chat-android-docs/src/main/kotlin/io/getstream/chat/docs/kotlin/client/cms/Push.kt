package io.getstream.chat.docs.kotlin.client.cms

import android.content.Context
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.models.Device
import io.getstream.chat.android.client.utils.Result
import io.getstream.chat.android.pushprovider.firebase.FirebaseMessagingDelegate

class Push(val context: Context, val client: ChatClient) {

    /**
     * @see <a href="https://getstream.io/chat/docs/push_android/?language=kotlin">Android & Firebase</a>
     */
    inner class AndroidAndFirebase {

        /**
         * @see <a href="https://getstream.io/chat/docs/android/push_android/?language=kotlin#handling-notifications-from-multiple-providers">Handling notifications from multiple providers</a>
         */
        inner class CustomFirebaseMessagingService : FirebaseMessagingService() {

            override fun onNewToken(token: String) {
                // Update device's token on Stream backend
                try {
                    FirebaseMessagingDelegate.registerFirebaseToken(token, "optional-provider-name")
                } catch (exception: IllegalStateException) {
                    // ChatClient was not initialized
                }
            }

            override fun onMessageReceived(message: RemoteMessage) {
                try {
                    if (FirebaseMessagingDelegate.handleRemoteMessage(message)) {
                        // RemoteMessage was from Stream and it is already processed
                    } else {
                        // RemoteMessage wasn't sent from Stream and it needs to be handled by you
                    }
                } catch (exception: IllegalStateException) {
                    // ChatClient was not initialized
                }
            }
        }
    }

    /**
     * @see <a href="https://getstream.io/chat/docs/push_devices/?language=kotlin">Device</a>
     */
    inner class Device_ {

        fun listDevices() {
            client.getDevices().enqueue { result ->
                when (result) {
                    is Result.Success -> {
                        val devices: List<Device> = result.value
                    }
                    is Result.Failure -> {
                        // Handler error
                    }
                }
            }
        }
    }
}
