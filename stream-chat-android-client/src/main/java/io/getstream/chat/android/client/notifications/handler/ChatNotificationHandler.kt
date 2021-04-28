package io.getstream.chat.android.client.notifications.handler

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.graphics.Color
import android.os.Build
import androidx.annotation.RequiresApi
import com.google.firebase.FirebaseApp
import com.google.firebase.iid.FirebaseInstanceId
import com.google.firebase.messaging.RemoteMessage
import io.getstream.chat.android.client.events.ChatEvent
import io.getstream.chat.android.client.logger.ChatLogger
import io.getstream.chat.android.client.notifications.DeviceRegisteredListener
import io.getstream.chat.android.client.notifications.FirebaseMessageParser
import io.getstream.chat.android.client.notifications.FirebaseMessageParserImpl
import io.getstream.chat.android.client.notifications.NotificationLoadDataListener

public open class ChatNotificationHandler @JvmOverloads constructor(
    protected val context: Context,
    public val config: NotificationConfig = NotificationConfig()
) {
    private val logger = ChatLogger.get("ChatNotificationHandler")
    private val firebaseMessageParserImpl: FirebaseMessageParser by lazy { FirebaseMessageParserImpl(config) }

    public open fun onChatEvent(event: ChatEvent): Boolean {
        return false
    }

    public open fun onFirebaseMessage(message: RemoteMessage): Boolean {
        return false
    }

    public open fun getDeviceRegisteredListener(): DeviceRegisteredListener? {
        return null
    }

    public open fun getDataLoadListener(): NotificationLoadDataListener? {
        return null
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public open fun createNotificationChannel(): NotificationChannel {
        logger.logI("createNotificationChannel()")
        return NotificationChannel(
            getNotificationChannelId(),
            getNotificationChannelName(),
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            setShowBadge(true)
            importance = NotificationManager.IMPORTANCE_HIGH
            enableLights(true)
            lightColor = Color.RED
            enableVibration(true)
            vibrationPattern = longArrayOf(
                100,
                200,
                300,
                400,
                500,
                400,
                300,
                200,
                400
            )
        }
    }

    public open fun getNotificationChannelId(): String = context.getString(config.notificationChannelId)

    public open fun getNotificationChannelName(): String =
        context.getString(config.notificationChannelName)

    public open fun getFirebaseMessageIdKey(): String = config.firebaseMessageIdKey
    public open fun getFirebaseChannelIdKey(): String = config.firebaseChannelIdKey
    public open fun getFirebaseChannelTypeKey(): String = config.firebaseChannelTypeKey

    public open fun getFirebaseMessageParser(): FirebaseMessageParser = firebaseMessageParserImpl
    internal fun isValidRemoteMessage(message: RemoteMessage): Boolean = getFirebaseMessageParser().isValidRemoteMessage(message)

    public open fun getFirebaseInstanceId(): FirebaseInstanceId? =
        if (config.useProvidedFirebaseInstance && FirebaseApp.getApps(context).isNotEmpty()) {
            FirebaseInstanceId.getInstance()
        } else {
            null
        }
}
