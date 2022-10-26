package io.getstream.chat.docs.kotlin.ui.guides

import android.content.Context
import androidx.startup.AppInitializer
import com.getstream.sdk.chat.startup.ThreeTenInitializer
import io.getstream.chat.android.ui.common.ChatUIInitializer

/**
 * [App Startup Initializers](https://getstream.io/chat/docs/sdk/android/ui/guides/app-startup-initializers/)
 */
class AppStartupInitializers {

    fun manualInitialization(appContext: Context) {
        AppInitializer.getInstance(appContext).run {
            initializeComponent(ThreeTenInitializer::class.java)
            initializeComponent(ChatUIInitializer::class.java)
        }
    }
}
