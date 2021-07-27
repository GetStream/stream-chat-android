package io.getstream.chat.ui.sample.application

import android.app.Application
import android.os.Build
import coil.Coil
import coil.ImageLoader
import coil.decode.GifDecoder
import coil.decode.ImageDecoderDecoder
import io.getstream.chat.ui.sample.data.user.SampleUser
import io.getstream.chat.ui.sample.data.user.UserRepository

class App : Application() {

    val offlinePluginEnabled: Boolean = false

    // done for simplicity, a DI framework should be used in the real app
    private val chatInitializer = ChatInitializer(this)
    private val chatInitializerV2 = ChatInitializerV2(this)
    val userRepository = UserRepository(this)

    fun initializeSDK(apiKey: String) {
        if (offlinePluginEnabled) chatInitializerV2.init(apiKey) else chatInitializer.init(apiKey)
    }

    override fun onCreate() {
        super.onCreate()
        initializeSDK(getApiKey())
        instance = this
        DebugMetricsHelper.init()
        Coil.setImageLoader(
            ImageLoader.Builder(this).componentRegistry {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    add(ImageDecoderDecoder(this@App))
                } else {
                    add(GifDecoder())
                }
            }.build()
        )
        ApplicationConfigurator.configureApp(this)
    }

    private fun getApiKey(): String {
        val user = userRepository.getUser()
        return if (user != SampleUser.None) {
            user.apiKey
        } else {
            AppConfig.apiKey
        }
    }

    companion object {
        lateinit var instance: App
            private set
    }
}
