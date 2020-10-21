package io.getstream.chat.sample.feature.users

import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val usersModule = module {
    viewModel { UsersViewModel(get(), get()) }
}
