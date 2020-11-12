package io.getstream.chat.sample.application

import android.app.Application
import io.getstream.chat.sample.data.user.UserRepository

class App : Application() {

    // done for simplicity, a DI framework should be used in the real app
    val chatInitializer = ChatInitializer(this)
    var userRepository = UserRepository()

    override fun onCreate() {
        instance = this
        super.onCreate()
        DebugMetricsHelper().init()
        ExtraDependenciesImpl().config(this)
    }

    companion object {
        lateinit var instance: App
            private set
    }

}
