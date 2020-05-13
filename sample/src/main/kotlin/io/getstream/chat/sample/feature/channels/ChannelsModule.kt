package io.getstream.chat.sample.feature.channels

import com.getstream.sdk.chat.viewmodel.ChannelsViewModelImpl
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val channelsModule = module {
    viewModel { ChannelsViewModelImpl() }
}