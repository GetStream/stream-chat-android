package io.getstream.chat.sample.feature.create_channel

import com.getstream.sdk.chat.viewmodel.CreateChannelViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val createChannelModule = module {
    viewModel { CreateChannelViewModel() }
}