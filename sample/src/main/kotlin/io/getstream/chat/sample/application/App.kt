package io.getstream.chat.sample.application

import android.app.Application
import com.bumptech.glide.Glide
import io.getstream.chat.sample.BuildConfig
import io.getstream.chat.sample.data.dataModule
import io.getstream.chat.sample.feature.channels.channelsModule
import loginModule
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin

class App: Application() {
    override fun onCreate() {
        super.onCreate()
        initKoin()
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
                    channelsModule
            ))
        }
    }
}