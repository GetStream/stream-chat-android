package io.getstream.chat.sample.feature.custom_login

import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val customLoginModule = module {
    viewModel { CustomLoginViewModel(get(), get()) }
}
