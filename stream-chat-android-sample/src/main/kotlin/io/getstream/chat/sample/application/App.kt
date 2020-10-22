package io.getstream.chat.sample.application

import android.app.Application
import io.getstream.chat.sample.BuildConfig
import io.getstream.chat.sample.data.dataModule
import io.getstream.chat.sample.feature.login.loginModule
import io.getstream.chat.sample.feature.users.usersModule
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin

class App : Application() {

    override fun onCreate() {
        super.onCreate()
        DebugMetricsHelper().init()
        initKoin()
    }

    private fun initKoin() {
        startKoin {
            if (BuildConfig.DEBUG) {
                androidLogger()
            }
            androidContext(this@App)

            // see crash/bug here: https://github.com/InsertKoinIO/koin/issues/871
            koin.loadModules(
                listOf(
                    appModule,
                    dataModule,
                    usersModule,
                    loginModule
                )
            )
            koin.createRootScope()
        }
    }
}
