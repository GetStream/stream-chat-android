@file:Suppress("DEPRECATION_ERROR")
package io.getstream.chat.docs.kotlin

import android.content.Context
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.models.Device
import io.getstream.chat.android.client.models.PushProvider
import io.getstream.chat.android.pushprovider.firebase.FirebaseMessagingDelegate

class Push(val context: Context, val client: ChatClient) {

    /**
     * @see <a href="https://getstream.io/chat/docs/push_android/?language=kotlin">Android & Firebase</a>
     */
    inner class AndroidAndFirebase {

        /**
         * @see <a href="https://getstream.io/chat/docs/push_android/?language=kotlin#registering-a-device-at-stream-backend">Registering a device at Stream Backend</a>
         */
        fun registeringDevice() {
            client.addDevice(
                Device(
                    token = "push-provider-token",
                    pushProvider = PushProvider.FIREBASE,
                )
            ).enqueue { result ->
                if (result.isSuccess) {
                    // Device was successfully registered
                } else {
                    // Handle result.error()
                }
            }
        }

        /**
         * @see <a href="https://getstream.io/chat/docs/android/push_android/?language=kotlin#handling-notifications-from-multiple-providers">Handling notifications from multiple providers</a>
         */
        inner class CustomFirebaseMessagingService : FirebaseMessagingService() {

            override fun onNewToken(token: String) {
                // Update device's token on Stream backend
                try {
                    FirebaseMessagingDelegate.registerFirebaseToken(token)
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

        /**
         * @see <a href="https://getstream.io/chat/docs/push_devices/?language=kotlin#register-a-device">Register a Device</a>
         */
        fun registerADevice() {
            client.addDevice(
                Device(
                    token = "push-provider-token",
                    pushProvider = PushProvider.FIREBASE,
                )
            ).enqueue { result ->
                if (result.isSuccess) {
                    // Device was successfully registered
                } else {
                    // Handle result.error()
                }
            }
        }

        /**
         * @see <a href="https://getstream.io/chat/docs/push_devices/?language=kotlin#unregister-a-device">Unregister a Device</a>
         */
        fun unregisterADevice() {
            client.deleteDevice(
                Device(
                    token = "push-provider-token",
                    pushProvider = PushProvider.FIREBASE,
                )
            ).enqueue { result ->
                if (result.isSuccess) {
                    // Device was successfully unregistered
                } else {
                    // Handle result.error()
                }
            }
        }

        fun listDevices() {
            client.getDevices().enqueue { result ->
                if (result.isSuccess) {
                    val devices: List<Device> = result.data()
                } else {
                    // Handle result.error()
                }
            }
        }
    }
}
