package io.getstream.chat.sample.feature.channels

import androidx.lifecycle.ViewModelProvider
import com.getstream.sdk.chat.viewmodel.ChannelListViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val channelsModule = module {
    viewModel { ViewModelProvider.AndroidViewModelFactory.getInstance(get()).create(ChannelListViewModel::class.java) }
}