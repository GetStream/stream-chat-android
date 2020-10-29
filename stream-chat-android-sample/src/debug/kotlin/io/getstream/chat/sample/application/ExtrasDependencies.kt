package io.getstream.chat.sample.application

import android.app.Application
import com.facebook.flipper.android.AndroidFlipperClient
import com.facebook.flipper.android.utils.FlipperUtils
import com.facebook.flipper.plugins.databases.DatabasesFlipperPlugin
import com.facebook.flipper.plugins.inspector.DescriptorMapping
import com.facebook.flipper.plugins.inspector.InspectorFlipperPlugin
import com.facebook.flipper.plugins.network.NetworkFlipperPlugin
import com.facebook.soloader.SoLoader

class ExtrasDependenciesImpl: ExtraDependencies {

    override fun configFlipper(application: Application) {
        SoLoader.init(application, false)

        if (FlipperUtils.shouldEnableFlipper(application)) {
            AndroidFlipperClient.getInstance(application).apply {
                addPlugin(InspectorFlipperPlugin(application, DescriptorMapping.withDefaults()))
                addPlugin(DatabasesFlipperPlugin(application))
                addPlugin(NetworkFlipperPlugin())
            }.start()
        }
    }
}
