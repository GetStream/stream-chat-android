package io.getstream.videosample.application

import android.app.Application
import io.getstream.chat.android.client.utils.internal.toggle.ToggleService
import io.getstream.chat.android.core.internal.InternalStreamChatApi
import io.getstream.videosample.BuildConfig

class App : Application() {

    // done for simplicity, a DI framework should be used in the real app
    private val chatInitializer = ChatInitializer(this)

    override fun onCreate() {
        super.onCreate()
        chatInitializer.init(getApiKey())
        instance = this
        initializeToggleService()
    }

    private fun getApiKey(): String {
        return AppConfig.getUser().apiKey
    }

    @OptIn(InternalStreamChatApi::class)
    private fun initializeToggleService() {
        ToggleService.init(applicationContext, mapOf(ToggleService.TOGGLE_KEY_OFFLINE to BuildConfig.DEBUG))
    }

    companion object {
        lateinit var instance: App
            private set
    }
}
