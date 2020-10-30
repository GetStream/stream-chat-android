package io.getstream.chat.ui.sample.application

import android.app.Application
import io.getstream.chat.ui.sample.data.user.UserRepository

class App : Application() {

    // this is done for simplicity, a DI framework should be used in a real app
    lateinit var chatInitializer: ChatInitializer
    lateinit var userRepository: UserRepository

    override fun onCreate() {
        super.onCreate()
        instance = this

        chatInitializer = ChatInitializer(this)
        userRepository = UserRepository()

        DebugMetricsHelper.init()
    }

    companion object {
        lateinit var instance: App
            private set
    }
}
