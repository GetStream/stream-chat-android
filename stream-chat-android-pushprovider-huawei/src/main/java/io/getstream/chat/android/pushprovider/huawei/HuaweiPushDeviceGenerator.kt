package io.getstream.chat.android.pushprovider.huawei

import android.content.Context
import com.huawei.hms.aaid.HmsInstanceId
import com.huawei.hms.api.ConnectionResult
import com.huawei.hms.api.HuaweiApiAvailability
import io.getstream.chat.android.client.logger.ChatLogger
import io.getstream.chat.android.client.models.Device
import io.getstream.chat.android.client.models.PushProvider
import io.getstream.chat.android.client.notifications.handler.PushDeviceGenerator
import io.getstream.chat.android.core.internal.coroutines.DispatcherProvider
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

public class HuaweiPushDeviceGenerator(context: Context, private val appId: String) :
    PushDeviceGenerator {
    private val hmsInstanceId: HmsInstanceId = HmsInstanceId.getInstance(context)
    private val logger = ChatLogger.get("ChatNotifications")

    override fun isValidForThisDevice(context: Context): Boolean =
        (
            HuaweiApiAvailability.getInstance()
                .isHuaweiMobileServicesAvailable(context) == ConnectionResult.SUCCESS
            ).also {
            logger.logI("Is Huawei available on on this device -> $it")
        }

    override fun asyncGenerateDevice(onDeviceGenerated: (device: Device) -> Unit) {
        logger.logI("Getting Huawei token")
        GlobalScope.launch(DispatcherProvider.IO) {
            hmsInstanceId.getToken(appId, "HCM")
                .takeUnless { it.isNullOrBlank() }
                ?.run {
                    logger.logI("Huawei returned token successfully")
                    onDeviceGenerated(
                        Device(
                            token = this,
                            pushProvider = PushProvider.HUAWEI,
                        )
                    )
                }
                ?: logger.logI("Error: Huawei didn't returned token")
        }
    }
}
