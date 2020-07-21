package io.getstream.chat.sample.application

import android.app.Application
import com.getstream.sdk.chat.Chat
import io.getstream.chat.sample.BuildConfig
import io.getstream.chat.sample.data.dataModule
import loginModule
import org.koin.android.ext.android.inject
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin

class App : Application() {
    private val appConfig: AppConfig by inject()
    private val notifications: NotificationsConfig by inject()

    override fun onCreate() {
        super.onCreate()
        DebugMetricsHelper().init()
        initKoin()
        Chat.Builder(appConfig.apiKey, this).apply {
            offlineEnabled = true
            notificationsConfig = notifications
        }.build()
    }

    private fun initKoin() {
        startKoin {
            if (BuildConfig.DEBUG) {
                androidLogger()
            }
            androidContext(this@App)
            modules(
                listOf(
                    appModule,
                    dataModule,
                    loginModule
                )
            )
        }
    }
}
