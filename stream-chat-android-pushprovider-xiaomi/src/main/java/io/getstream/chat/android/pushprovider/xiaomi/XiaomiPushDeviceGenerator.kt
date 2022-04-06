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
 
package io.getstream.chat.android.pushprovider.xiaomi

import android.content.Context
import com.xiaomi.mipush.sdk.MiPushClient
import io.getstream.chat.android.client.logger.ChatLogger
import io.getstream.chat.android.client.models.Device
import io.getstream.chat.android.client.notifications.handler.PushDeviceGenerator

/**
 * Generator responsible for providing information needed to register Xiaomi push notifications provider.
 *
 * @property appId The App ID for the app registered on Xiaomi Developer Console.
 * @property appKey The App Key for the app registered on Xiaomi Developer Console.
 */
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
