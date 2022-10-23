package io.getstream.chat.docs.java.ui.guides;

import android.content.Context;

import androidx.startup.AppInitializer;

import com.getstream.sdk.chat.startup.ThreeTenInitializer;

import io.getstream.chat.android.ui.common.ChatUIInitializer;

/**
 * [App Startup Initializers](https://getstream.io/chat/docs/sdk/android/ui/guides/app-startup-initializers/)
 */
public class AppStartupInitializers {

    public void manualInitialization(Context appContext) {
        AppInitializer.getInstance(appContext).initializeComponent(ThreeTenInitializer.class);
        AppInitializer.getInstance(appContext).initializeComponent(ChatUIInitializer.class);
    }
}
