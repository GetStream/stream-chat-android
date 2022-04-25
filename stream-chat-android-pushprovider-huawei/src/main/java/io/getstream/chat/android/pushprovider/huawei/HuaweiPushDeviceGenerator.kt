/*
 * Copyright (c) 2014-2022 Stream.io Inc. All rights reserved.
 *
 * Licensed under the Stream License;
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    https://github.com/GetStream/stream-chat-android/blob/main/LICENSE
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.getstream.chat.android.pushprovider.huawei

import android.content.Context
import com.huawei.hms.aaid.HmsInstanceId
import com.huawei.hms.api.ConnectionResult.SUCCESS
import com.huawei.hms.api.HuaweiApiAvailability
import io.getstream.chat.android.client.logger.ChatLogger
import io.getstream.chat.android.client.models.Device
import io.getstream.chat.android.client.models.PushProvider
import io.getstream.chat.android.client.notifications.handler.PushDeviceGenerator
import io.getstream.chat.android.core.internal.coroutines.DispatcherProvider
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

/**
 * Generator responsible for providing information needed to register Huawei push notifications provider
 */
public class HuaweiPushDeviceGenerator(context: Context, private val appId: String) :
    PushDeviceGenerator {
    private val hmsInstanceId: HmsInstanceId = HmsInstanceId.getInstance(context)
    private val logger = ChatLogger.get("ChatNotifications")

    override fun isValidForThisDevice(context: Context): Boolean =
        (HuaweiApiAvailability.getInstance().isHuaweiMobileServicesAvailable(context) == SUCCESS).also {
            logger.logI("Is Huawei available on on this device -> $it")
        }

    override fun asyncGenerateDevice(onDeviceGenerated: (device: Device) -> Unit) {
        logger.logI("Getting Huawei token")

        @OptIn(DelicateCoroutinesApi::class)
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
