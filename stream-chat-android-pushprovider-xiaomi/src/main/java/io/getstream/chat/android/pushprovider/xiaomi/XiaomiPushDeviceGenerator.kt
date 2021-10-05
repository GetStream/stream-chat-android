package io.getstream.chat.android.pushprovider.xiaomi

import android.content.Context
import com.xiaomi.mipush.sdk.MiPushClient
import io.getstream.chat.android.client.logger.ChatLogger
import io.getstream.chat.android.client.models.Device
import io.getstream.chat.android.client.notifications.handler.PushDeviceGenerator

public class XiaomiPushDeviceGenerator(context: Context, private val appId: String, private val appKey: String) :
    PushDeviceGenerator {
    private val appContext = context.applicationContext
    private val logger = ChatLogger.get("ChatNotifications")

    override fun isValidForThisDevice(context: Context): Boolean = true

    override fun asyncGenerateDevice(onDeviceGenerated: (device: Device) -> Unit) {
        logger.logI("Getting Xiaomi token")
        MiPushClient.registerPush(appContext, appId, appKey)
    }
}
