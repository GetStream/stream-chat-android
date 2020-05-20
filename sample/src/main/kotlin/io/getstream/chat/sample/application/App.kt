package io.getstream.chat.sample.application

import android.app.Application
import com.getstream.sdk.chat.Chat
import io.getstream.chat.sample.BuildConfig
import io.getstream.chat.sample.data.dataModule
import io.getstream.chat.sample.feature.channels.channelsModule
import io.getstream.chat.sample.feature.create_channel.createChannelModule
import loginModule
import org.koin.android.ext.android.inject
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import timber.log.Timber

class App : Application() {

    private val appConfig: AppConfig by inject()

    override fun onCreate() {
        super.onCreate()
        initKoin()
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }

        Chat.Builder(appConfig.apiKey, this).apply { offlineEnabled = true }.build()
    }

    private fun initKoin() {
        startKoin {
            if (BuildConfig.DEBUG) {
                androidLogger()
            }
            androidContext(this@App)
            modules(listOf(
                    appModule,
                    dataModule,
                    loginModule,
                    channelsModule,
                    createChannelModule
            ))
        }
    }
}