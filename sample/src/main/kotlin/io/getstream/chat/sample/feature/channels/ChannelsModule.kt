package io.getstream.chat.sample.feature.channels

import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val channelsModule = module {
    viewModel { ChannelsViewModel(get()) }
}