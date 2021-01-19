package io.getstream.chat.ui.sample.application

import android.app.Application
import com.facebook.flipper.android.AndroidFlipperClient
import com.facebook.flipper.android.utils.FlipperUtils
import com.facebook.flipper.plugins.databases.DatabasesFlipperPlugin
import com.facebook.flipper.plugins.inspector.DescriptorMapping
import com.facebook.flipper.plugins.inspector.InspectorFlipperPlugin
import com.facebook.soloader.SoLoader
import io.getstream.chat.android.client.di.networkFlipper

object ApplicationConfigurator {
    fun configureApp(application: Application) {
        SoLoader.init(application, false)

        if (FlipperUtils.shouldEnableFlipper(application)) {
            AndroidFlipperClient.getInstance(application).apply {
                addPlugin(InspectorFlipperPlugin(application, DescriptorMapping.withDefaults()))
                addPlugin(DatabasesFlipperPlugin(application))
                addPlugin(networkFlipper)
            }.start()
        }
    }
}
