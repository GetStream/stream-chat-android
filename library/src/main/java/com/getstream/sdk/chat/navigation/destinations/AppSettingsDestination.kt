package com.getstream.sdk.chat.navigation.destinations

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings

class AppSettingsDestination(context: Context) : ChatDestination(context) {
    override fun navigate() {
        val intent = Intent().apply {
            action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
            data = Uri.fromParts("package", context.packageName, null)
        }
        start(intent)
    }
}
