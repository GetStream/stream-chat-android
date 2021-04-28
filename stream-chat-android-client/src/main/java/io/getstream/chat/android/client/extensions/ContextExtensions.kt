package io.getstream.chat.android.client.extensions

import android.content.ComponentName
import android.content.Context
import android.content.Intent

internal fun Context.sendImplicitBroadcast(intent: Intent) {
    packageManager
        .queryBroadcastReceivers(intent, 0)
        .maxByOrNull { it.priority }
        ?.let {
            val explicitIntent = Intent(intent)
            explicitIntent.component = ComponentName(it.activityInfo.applicationInfo.packageName, it.activityInfo.name)
            sendBroadcast(explicitIntent)
        }
}
