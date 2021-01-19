package io.getstream.chat.ui.sample.application

import android.app.Application
import android.os.Build
import coil.Coil
import coil.ImageLoader
import coil.decode.GifDecoder
import coil.decode.ImageDecoderDecoder
import io.getstream.chat.ui.sample.data.user.UserRepository

class App : Application() {

    // done for simplicity, a DI framework should be used in the real app
    val chatInitializer = ChatInitializer(this)
    val userRepository = UserRepository(this)

    override fun onCreate() {
        super.onCreate()
        instance = this
        DebugMetricsHelper.init()
        Coil.setImageLoader(
            ImageLoader.Builder(this).componentRegistry {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    add(ImageDecoderDecoder())
                } else {
                    add(GifDecoder())
                }
            }.build()
        )
        ApplicationConfigurator.configureApp(this)
    }

    companion object {
        lateinit var instance: App
            private set
    }
}
