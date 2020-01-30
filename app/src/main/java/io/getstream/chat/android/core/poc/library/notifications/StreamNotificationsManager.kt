package io.getstream.chat.android.core.poc.library.notifications

import android.content.Context
import android.util.Log
import com.google.firebase.messaging.RemoteMessage
import io.getstream.chat.android.core.poc.library.ChatApi
import io.getstream.chat.android.core.poc.library.events.ChatEvent
import io.getstream.chat.android.core.poc.library.models.StreamNotification
import io.getstream.chat.android.core.poc.library.notifications.options.NotificationOptions
import io.getstream.chat.android.core.poc.library.rest.AddDeviceRequest
import java.util.*

class StreamNotificationsManager constructor(
    val notificationOptions: NotificationOptions,
    val registerListener: DeviceRegisteredListener? = null,
    val api: ChatApi
) : NotificationsManager {

    private val DEFAULT_REQUEST_CODE = 999
    val CHANNEL_ID_KEY = "id"
    val CHANNEL_TYPE_KEY = "type"
    private val FIREBASE_MESSAGE_ID_KEY = "message_id"

    private val notificationsMap: HashMap<String, StreamNotification> = HashMap()

    private val TAG = StreamNotificationsManager::class.java.simpleName

    var failMessageListener: NotificationMessageLoadListener? = null

    override fun setFirebaseToken(firebaseToken: String, context: Context) {
        Log.d(TAG, "setFirebaseToken: $firebaseToken")

        api.addDevice(
            request = AddDeviceRequest(
                id = firebaseToken
            )
        ).enqueue { result ->
            if (result.isSuccess) {
                registerListener?.onDeviceRegisteredSuccess()
                Log.i(TAG, "DeviceRegisteredSuccess")
            } else {
                registerListener?.onDeviceRegisteredError(result.error())
                Log.e(TAG, "Error register device ${result.error().message}")
            }
        }
    }

    override fun onReceiveFirebaseMessage(remoteMessage: RemoteMessage, context: Context) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onReceiveWebSocketEvent(event: ChatEvent, context: Context) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun handleRemoteMessage(context: Context?, remoteMessage: RemoteMessage?) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun handleEvent(context: Context?, event: ChatEvent?) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}