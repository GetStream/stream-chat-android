package io.getstream.chat.ui.sample.application

import android.app.Application
import io.getstream.chat.ui.sample.data.user.UserRepository

class App : Application() {

    // done for simplicity, a DI framework should be used in the real app
    val chatInitializer = ChatInitializer(this)
    val userRepository = UserRepository()

    override fun onCreate() {
        super.onCreate()
        instance = this
        DebugMetricsHelper.init()
    }

    companion object {
        lateinit var instance: App
            private set
    }
}
