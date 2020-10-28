package io.getstream.chat.sample.feature.user_login

import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val userLoginModule = module {
    viewModel { UserLoginViewModel(get(), get(), get()) }
}
