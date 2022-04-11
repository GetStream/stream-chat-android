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

package io.getstream.chat.android.pushprovider.firebase

import android.content.Context
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.firebase.messaging.FirebaseMessaging
import io.getstream.chat.android.client.logger.ChatLogger
import io.getstream.chat.android.client.models.Device
import io.getstream.chat.android.client.models.PushProvider
import io.getstream.chat.android.client.notifications.handler.PushDeviceGenerator

/**
 * Generator responsible for providing information needed to register Firebase push notifications provider
 */
public class FirebasePushDeviceGenerator(
    private val firebaseMessaging: FirebaseMessaging = FirebaseMessaging.getInstance(),
) : PushDeviceGenerator {
    private val logger = ChatLogger.get("ChatNotifications")

    override fun isValidForThisDevice(context: Context): Boolean =
        (GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(context) == ConnectionResult.SUCCESS).also {
            logger.logI("Is Firebase available on on this device -> $it")
        }

    override fun asyncGenerateDevice(onDeviceGenerated: (device: Device) -> Unit) {
        logger.logI("Getting Firebase token")
        firebaseMessaging.token.addOnCompleteListener {
            if (it.isSuccessful) {
                logger.logI("Firebase returned token successfully")
                onDeviceGenerated(
                    Device(
                        token = it.result,
                        pushProvider = PushProvider.FIREBASE,
                    )
                )
            } else {
                logger.logI("Error: Firebase didn't returned token")
            }
        }
    }
}
