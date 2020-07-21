package io.getstream.chat.sample.data

import io.getstream.chat.sample.data.user.UserRepository
import org.koin.dsl.module

val dataModule = module {
    single { UserRepository() }
}
