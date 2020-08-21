package io.getstream.chat.sample.application

import org.koin.dsl.module

val appModule = module {
    single { AppConfig() }
}
