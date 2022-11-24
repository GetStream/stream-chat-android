package io.getstream.chat.android.client.notifications

import io.getstream.android.push.PushDelegate
import io.getstream.android.push.PushDevice
import io.getstream.android.push.PushProvider
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.PayloadValidator
import io.getstream.chat.android.models.Device
import io.getstream.chat.android.models.PushMessage
private typealias DevicePushProvider = io.getstream.chat.android.models.PushProvider

internal class ChatPushDelegate : PushDelegate {
    override fun handlePushMessage(payload: Map<String, Any?>): Boolean =
        payload.ifValid {
            ChatClient.handlePushMessage(
                PushMessage(
                    channelId = payload.getValue("channel_id") as String,
                    messageId = payload.getValue("message_id") as String,
                    channelType = payload.getValue("channel_type") as String,
                )
            )
        }

    override fun registerPushDevice(pushDevice: PushDevice) {
        ChatClient.setDevice(pushDevice.toDevice())
    }

    private fun PushDevice.toDevice(): Device =
        Device(
            token = token,
            pushProvider = pushProvider.toDevicePushProvider(),
            providerName = providerName,
        )

    private fun PushProvider.toDevicePushProvider(): DevicePushProvider = when (this) {
        PushProvider.FIREBASE -> DevicePushProvider.FIREBASE
        PushProvider.HUAWEI -> DevicePushProvider.HUAWEI
        PushProvider.XIAOMI -> DevicePushProvider.XIAOMI
        PushProvider.UNKNOWN -> DevicePushProvider.UNKNOWN
    }

    private fun Map<String, Any?>.ifValid(effect: () -> Unit): Boolean {
        val isValid = PayloadValidator.isFromStreamServer(this)
            && PayloadValidator.isValidNewMessage(this)
        effect.takeIf { isValid }?.invoke()
        return isValid
    }
}