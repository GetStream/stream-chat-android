package io.getstream.chat.docs.kotlin.ui.guides

import android.content.Context
import androidx.startup.AppInitializer
import io.getstream.chat.android.ui.initializer.ChatUIInitializer

/**
 * [App Startup Initializers](https://getstream.io/chat/docs/sdk/android/ui/guides/app-startup-initializers/)
 */
class AppStartupInitializers {

    fun manualInitialization(appContext: Context) {
        AppInitializer.getInstance(appContext).initializeComponent(ChatUIInitializer::class.java)
    }
}
