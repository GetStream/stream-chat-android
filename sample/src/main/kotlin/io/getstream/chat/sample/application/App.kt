package io.getstream.chat.sample.application

import android.app.Application
import com.getstream.sdk.chat.Chat
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.models.User
import io.getstream.chat.android.livedata.ChatDomain
import io.getstream.chat.sample.BuildConfig
import io.getstream.chat.sample.data.dataModule
import io.getstream.chat.sample.feature.channels.channelsModule
import loginModule
import org.koin.android.ext.android.inject
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import org.koin.dsl.module
import timber.log.Timber

class App : Application() {

    private val appConfig: AppConfig by inject()

    override fun onCreate() {
        super.onCreate()
        initKoin()
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }

        val chatClient = ChatClient.Builder(appConfig.apiKey, this).build()
        val emptyUser = User() // TODO: make user arg in ChatDomain.Builder default or remove it. Shouldn't the ChatDomain be getting user from ChatClient?
        val chatDomain = ChatDomain.Builder(this, chatClient, emptyUser)
                .offlineEnabled()
                .userPresenceEnabled()
                .build()

        Chat.Builder(this)
                .chatDomain(chatDomain)
                .build()
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
                    module {
                        single { Chat.getInstance() }
                    }
            ))
        }
    }
}