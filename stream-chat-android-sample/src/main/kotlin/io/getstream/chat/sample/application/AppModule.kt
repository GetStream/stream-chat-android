package io.getstream.chat.sample.application

import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val appModule = module {
    single { AppConfig() }
    single { ChatInitializer(androidContext()) }
}
