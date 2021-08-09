package io.getstream.chat.android.client.notifications

import android.content.Context
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.firebase.messaging.FirebaseMessaging
import io.getstream.chat.android.client.logger.ChatLogger
import io.getstream.chat.android.client.models.Device
import io.getstream.chat.android.client.models.PushProvider
import io.getstream.chat.android.client.notifications.handler.PushDeviceGenerator

public class FirebasePushDeviceGenerator(private val firebaseMessaging: FirebaseMessaging = FirebaseMessaging.getInstance()) :
    PushDeviceGenerator {
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
